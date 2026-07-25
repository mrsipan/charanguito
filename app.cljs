;; Charango Chords Pro — Andean String Instrument Library
;; squint cljs + reagami + bulma
;; Charango: 10 strings in 5 courses (pairs), tuned E-A-E-C-G (high to low, re-entrant)
;; The 3rd course (middle E) traditionally has octave separation: E4 + E3 for richer tone.
;; Each course drawn as double lines in diagrams to show the paired strings.
;; Octave mode toggle controls whether course 3 plays the octave pair in audio.

(require '["https://unpkg.com/reagami@0.1.37/reagami.mjs" :as rg])

;; ── overflow fix for standalone export ───────────────────────────
(set! (.. js/document -body -style -overflow) "auto")
(when-let [app-el (js/document.getElementById "app")]
  (set! (.-overflow (.-style app-el)) "auto"))

;; ── Constants ─────────────────────────────────────────────────────
(def all-notes ["C" "C#" "D" "D#" "E" "F" "F#" "G" "G#" "A" "A#" "B"])
;; Charango: 5 courses, 0=1st course (highest E) to 4=5th course (lowest G)
(def course-open-notes ["E" "A" "E" "C" "G"])
(def course-names ["1st (E-E)" "2nd (A-A)" "3rd (E-e) oct" "4th (C-C)" "5th (G-G)"])
(def course-count 5)

(defn note-index [n]
  (let [idx (.indexOf all-notes n)]
    (if (>= idx 0) idx -1)))

(defn transpose-note [note semitones]
  (let [idx (note-index note)]
    (if (>= idx 0)
      (get all-notes (mod (+ idx semitones) 12))
      note)))

(defn chord-name-transpose [chord-name semitones]
  (if (= semitones 0) chord-name
    (let [len (count chord-name)
          second-char (if (> len 1) (.charAt chord-name 1) "")
          root (if (or (= second-char "#") (= second-char "b"))
                 (.substring chord-name 0 2)
                 (.substring chord-name 0 1))
          rest-suffix (.substring chord-name (count root))
          new-root (transpose-note root semitones)]
      (str new-root rest-suffix))))

;; ── Audio Context ─────────────────────────────────────────────────
(def audio-ctx (atom nil))
(defn get-audio-ctx []
  (when-not @audio-ctx
    (reset! audio-ctx (js/AudioContext.)))
  (when @audio-ctx
    (let [ctx @audio-ctx]
      (when (= (.-state ctx) "suspended")
        (.resume ctx))
      ctx)))

;; ── Curated Charango Chords ───────────────────────────────────────
;; fingering: [[course fret finger] ...]  course 1=high-E to 5=low-G
;; fret: -1=muted, 0=open, >0=fretted. finger: -1=none, 1-4=finger

(def curated-charango-chords
  {"C"    {:name "C"    :root "C"  :type "major"     :intervals [0 4 7]
           :fingering [[1 0 -1] [2 3 3] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "G"    {:name "G"    :root "G"  :type "major"     :intervals [0 4 7]
           :fingering [[1 0 -1] [2 2 2] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "Am"   {:name "Am"   :root "A"  :type "minor"     :intervals [0 3 7]
           :fingering [[1 0 -1] [2 0 -1] [3 0 -1] [4 0 -1] [5 2 2]]
           :difficulty "beginner"}
   "F"    {:name "F"    :root "F"  :type "major"     :intervals [0 4 7]
           :fingering [[1 1 1] [2 1 1] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "E"    {:name "E"    :root "E"  :type "major"     :intervals [0 4 7]
           :fingering [[1 0 -1] [2 0 -1] [3 1 1] [4 0 -1] [5 1 2]]
           :difficulty "beginner"}
   "Dm"   {:name "Dm"   :root "D"  :type "minor"     :intervals [0 3 7]
           :fingering [[1 1 1] [2 3 3] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "Em"   {:name "Em"   :root "E"  :type "minor"     :intervals [0 3 7]
           :fingering [[1 0 -1] [2 0 -1] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "D7"   {:name "D7"   :root "D"  :type "dominant7" :intervals [0 4 7 10]
           :fingering [[1 1 2] [2 3 3] [3 0 -1] [4 2 1] [5 0 -1]]
           :difficulty "beginner"}
   "G7"   {:name "G7"   :root "G"  :type "dominant7" :intervals [0 4 7 10]
           :fingering [[1 0 -1] [2 1 1] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "A7"   {:name "A7"   :root "A"  :type "dominant7" :intervals [0 4 7 10]
           :fingering [[1 0 -1] [2 2 2] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "C7"   {:name "C7"   :root "C"  :type "dominant7" :intervals [0 4 7 10]
           :fingering [[1 0 -1] [2 2 2] [3 0 -1] [4 0 -1] [5 3 3]]
           :difficulty "beginner"}
   "Bm"   {:name "Bm"   :root "B"  :type "minor"     :intervals [0 3 7]
           :fingering [[1 2 3] [2 2 2] [3 0 -1] [4 0 -1] [5 2 1]]
           :difficulty "intermediate"}
   "Am7"  {:name "Am7"  :root "A"  :type "minor7"    :intervals [0 3 7 10]
           :fingering [[1 0 -1] [2 0 -1] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "D"    {:name "D"    :root "D"  :type "major"     :intervals [0 4 7]
           :fingering [[1 2 1] [2 3 2] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "A"    {:name "A"    :root "A"  :type "major"     :intervals [0 4 7]
           :fingering [[1 0 -1] [2 0 -1] [3 0 -1] [4 4 3] [5 2 1]]
           :difficulty "intermediate"}
   "Cm"   {:name "Cm"   :root "C"  :type "minor"     :intervals [0 3 7]
           :fingering [[1 0 -1] [2 3 3] [3 0 -1] [4 1 1] [5 0 -1]]
           :difficulty "intermediate"}
   "Fm"   {:name "Fm"   :root "F"  :type "minor"     :intervals [0 3 7]
           :fingering [[1 1 1] [2 1 1] [3 0 -1] [4 1 2] [5 0 -1]]
           :difficulty "intermediate"}
   "E7"   {:name "E7"   :root "E"  :type "dominant7" :intervals [0 4 7 10]
           :fingering [[1 0 -1] [2 0 -1] [3 1 1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}
   "Gm"   {:name "Gm"   :root "G"  :type "minor"     :intervals [0 3 7]
           :fingering [[1 0 -1] [2 1 1] [3 0 -1] [4 0 -1] [5 0 -1]]
           :difficulty "beginner"}})

;; ── Chord Type Definitions ────────────────────────────────────────
(def chord-type-defs
  {"major"     [0 4 7]
   "minor"     [0 3 7]
   "dominant7" [0 4 7 10]
   "maj7"      [0 4 7 11]
   "minor7"    [0 3 7 10]
   "sus2"      [0 2 7]
   "sus4"      [0 5 7]
   "dim"       [0 3 6]
   "dim7"      [0 3 6 9]
   "aug"       [0 4 8]
   "m7b5"      [0 3 6 10]
   "add9"      [0 4 7 2]
   "9th"       [0 4 7 10 2]
   "maj9"      [0 4 7 11 2]
   "m9"        [0 3 7 10 2]
   "6th"       [0 4 7 9]
   "m6"        [0 3 7 9]
   "7#9"       [0 4 7 10 3]
   "7b9"       [0 4 7 10 1]
   "13th"      [0 4 7 10 2 9]})

(def chord-name-suffix
  {"major" "" "minor" "m" "dominant7" "7" "maj7" "maj7" "minor7" "m7"
   "sus2" "sus2" "sus4" "sus4" "dim" "dim" "dim7" "dim7" "aug" "aug"
   "m7b5" "m7b5" "add9" "add9" "9th" "9" "maj9" "maj9" "m9" "m9"
   "6th" "6" "m6" "m6" "7#9" "7#9" "7b9" "7b9" "13th" "13"})

;; ── Fretboard Solver (5-course charango) ──────────────────────────

(defn fret->note-solver [course-idx fret]
  (let [open-note (get course-open-notes course-idx)
        open-idx (note-index open-note)]
    (get all-notes (mod (+ open-idx fret) 12))))

(defn solver-valid-frets [course-idx required-notes max-fret]
  (let [results (array)]
    (.push results [-1 nil])
    (dotimes [fret (inc max-fret)]
      (let [note (fret->note-solver course-idx fret)]
        (when (contains? required-notes note)
          (.push results [fret note]))))
    (vec results)))

(defn solver-chord-notes [root intervals]
  (let [root-idx (note-index root)
        notes (array)]
    (dotimes [i (count intervals)]
      (.push notes (get all-notes (mod (+ root-idx (get intervals i)) 12))))
    (into #{} (vec notes))))

(defn solver-score [fingering]
  (let [fretted (filterv (fn [s] (> (get s 1) 0)) fingering)
        num-fretted (count fretted)
        num-muted (count (filterv (fn [s] (= (get s 1) -1)) fingering))
        num-played (- course-count num-muted)]
    (if (zero? num-fretted)
      9999
      (let [frets (mapv (fn [s] (get s 1)) fretted)
            min-fret (apply min frets)
            max-fret (apply max frets)
            span (- max-fret min-fret)]
        (+ (* span 8) (* num-muted 6) (* num-played -8) max-fret)))))

(defn solver-find-fingering [required-notes max-fret]
  (let [string-options (mapv (fn [ci]
                                (solver-valid-frets ci required-notes max-fret))
                              (range course-count))
        best (atom nil)
        best-score (atom 99999)
        mf (or max-fret 12)]
    (letfn [(search [course-idx assignment covered-notes]
              (if (= course-idx course-count)
                (when (= (count covered-notes) (count required-notes))
                  (let [score (solver-score assignment)]
                    (when (< score @best-score)
                      (reset! best-score score)
                      (reset! best (vec assignment)))))
                (let [options (get string-options course-idx)]
                  (dotimes [i (count options)]
                    (let [[fret note] (get options i)
                          new-assignment (conj assignment [course-idx fret note])
                          new-covered (if (and note (contains? required-notes note))
                                        (conj covered-notes note)
                                        covered-notes)]
                      (search (inc course-idx) new-assignment new-covered))))))]
      (search 0 [] #{}))
    (when-let [bf @best]
      (let [fretted (filterv (fn [s] (> (get s 1) 0)) bf)
            sorted-by-fret (sort-by (fn [s] (get s 1)) fretted)
            finger-map (into {}
                             (mapv (fn [i] [(get sorted-by-fret i) (+ i 1)])
                                   (range (min 4 (count sorted-by-fret)))))]
        {:fingering (mapv (fn [s]
                            (let [course-num (+ (get s 0) 1)
                                  fret (get s 1)
                                  finger (or (get finger-map s) -1)]
                              [course-num fret finger]))
                          bf)
         :score @best-score
         :span (let [frets (mapv (fn [s] (get s 1))
                                 (filterv (fn [s] (> (get s 1) 0)) bf))]
                 (if (seq frets) (- (apply max frets) (apply min frets)) 0))}))))

(defn generate-chord [root type-name]
  (when-let [intervals (get chord-type-defs type-name)]
    (let [suffix (get chord-name-suffix type-name type-name)
          name (str root suffix)
          notes (solver-chord-notes root intervals)
          result (or (solver-find-fingering notes 5)
                     (solver-find-fingering notes 12))]
      (when result
        (let [span (get result :span)
              difficulty (if (<= span 1) "beginner"
                             (if (<= span 2) "intermediate" "advanced"))]
          {:name name :root root :type type-name :intervals intervals
           :fingering (get result :fingering) :difficulty difficulty
           :auto-generated true})))))

(defn generate-all-chords []
  (let [roots ["C" "C#" "D" "D#" "E" "F" "F#" "G" "G#" "A" "A#" "B"]
        types (keys chord-type-defs)
        result (array)]
    (doseq [root roots]
      (doseq [type-name types]
        (when-let [chord (generate-chord root type-name)]
          (.push result [(get chord :name) chord]))))
    (into {} (vec result))))

;; ── Chord DB: start curated, async-generate rest ──────────────────
(def chords-db curated-charango-chords)

(js/setTimeout
  (fn []
    (try
      (let [auto (generate-all-chords)]
        ;; Override auto with curated
        (set! chords-db
          (reduce (fn [db k]
                    (assoc db k (get curated-charango-chords k)))
                  auto
                  (keys curated-charango-chords)))
        ;; Enharmonic aliases
        (doseq [[flat sharp] {"Bb" "A#" "Db" "C#" "Eb" "D#" "Gb" "F#" "Ab" "G#"}]
          (doseq [type-name (keys chord-type-defs)]
            (let [suffix (get chord-name-suffix type-name type-name)
                  flat-key (str flat suffix)
                  sharp-key (str sharp suffix)]
              (when-let [chord-data (get chords-db sharp-key)]
                (set! chords-db (assoc chords-db flat-key (assoc chord-data :name flat-key))))))))
      (catch js/Error e
        (println "Chord generation error:" (.-message e)))))
  100)

;; ── SVG Chord Diagram (5 courses) ─────────────────────────────────

(defn is-root-note? [chord course-idx fret]
  (let [root (get chord :root)]
    (= (fret->note-solver course-idx fret) root)))

(defn render-chord-svg
  ([chord] (render-chord-svg chord 140 180))
  ([chord w h]
   (let [fingering (get chord :fingering)
         top-margin 32 bottom-margin 12 left-margin 28 right-margin 12
         fret-area-left left-margin
         fret-area-top top-margin
         fret-area-right (- w right-margin)
         fret-area-bottom (- h bottom-margin)
         fret-area-width (- fret-area-right fret-area-left)
         fret-area-height (- fret-area-bottom fret-area-top)
         course-spacing (/ fret-area-width (- course-count 1))
         fret-spacing (/ fret-area-height 5)
         dot-radius 10
         fretted (mapv (fn [s] (get s 1))
                       (filterv (fn [s] (> (get s 1) 0)) fingering))
         min-fret (if (pos? (count fretted)) (apply min fretted) 1)
         start-fret (if (> min-fret 3) min-fret 1)]
     (let [title [:text {:x (/ w 2) :y 16 :text-anchor "middle"
                         :font-size "14" :font-weight "bold" :fill "#333"}
                  (get chord :name)]
           fret-marker (when (> start-fret 1)
                         [:text {:x (- fret-area-left 18)
                                 :y (+ fret-area-top (/ fret-spacing 2) 6)
                                 :text-anchor "middle"
                                 :font-size "12" :font-weight "bold" :fill "#666"}
                          (str start-fret "fr")])
           children (array)]
       (.push children title)
       (when fret-marker (.push children fret-marker))
       ;; Fret lines
       (dotimes [i 6]
         (let [y (+ fret-area-top (* i fret-spacing))]
           (.push children [:line {:x1 fret-area-left :y1 y :x2 fret-area-right :y2 y
                                    :stroke (if (= i 0) "#c4a35a" "#ccc")
                                    :stroke-width (if (= i 0) 3 1)}])))
       ;; Course lines (pairs — two closely-spaced lines per course to show paired strings)
       (dotimes [i course-count]
         (let [x (+ fret-area-left (* i course-spacing))
               pair-gap 3]  ;; gap between the two strings of a course
           (.push children [:line {:x1 (- x pair-gap) :y1 fret-area-top
                                    :x2 (- x pair-gap) :y2 fret-area-bottom
                                    :stroke "#555" :stroke-width 1}])
           (.push children [:line {:x1 (+ x pair-gap) :y1 fret-area-top
                                    :x2 (+ x pair-gap) :y2 fret-area-bottom
                                    :stroke "#555" :stroke-width 1}])))
       ;; Course markers + finger dots
       (dotimes [i course-count]
         (let [course-data (get fingering i)
               fret (get course-data 1)
               finger (get course-data 2)
               x (+ fret-area-left (* i course-spacing))]
           (cond
             (= fret -1)
             (.push children [:text {:x x :y (- top-margin 8)
                                      :text-anchor "middle"
                                      :font-size "12" :font-weight "bold"
                                      :fill "#e74c3c"} "X"])
             (= fret 0)
             (.push children [:text {:x x :y (- top-margin 8)
                                      :text-anchor "middle"
                                      :font-size "14" :font-weight "bold"
                                      :fill "#27ae60"} "O"])
             :else
             (let [adjusted-fret (- fret (- start-fret 1))
                   y (+ fret-area-top (* (- adjusted-fret 0.5) fret-spacing))
                   root-note? (is-root-note? chord i fret)]
               (.push children [:circle {:cx x :cy y :r dot-radius
                                          :fill (if root-note? "#f1c40f" "#2c3e50")
                                          :stroke (if root-note? "#e67e22" "#1a1a2e")
                                          :stroke-width "1.5"}])
               (when (> finger 0)
                 (.push children [:text {:x x :y (+ y 4)
                                          :text-anchor "middle"
                                          :font-size "11" :font-weight "bold"
                                          :fill (if root-note? "#333" "#fff")}
                                   finger]))))))
       (into [:svg {:xmlns "http://www.w3.org/2000/svg"
                    :width w :height h
                    :viewBox (str "0 0 " w " " h)
                    :style "display:block;margin:0 auto;"}]
             (.slice children 0))))))

;; ── Audio Playback (5 courses) ────────────────────────────────────
(def volume (atom 0.5))

(defn play-string [ctx freq start-time duration vol]
  (let [osc (.createOscillator ctx)
        gain-node (.createGain ctx)]
    (set! (.-type osc) "triangle")
    (set! (.. osc -frequency -value) freq)
    (set! (.. gain-node -gain -value) 0)
    (.connect osc gain-node)
    (.connect gain-node (.-destination ctx))
    (.setValueAtTime (.. gain-node -gain) 0 start-time)
    (.linearRampToValueAtTime (.. gain-node -gain) (* vol 0.3) (+ start-time 0.01))
    (.exponentialRampToValueAtTime (.. gain-node -gain) 0.001 (+ start-time duration))
    (.start osc start-time)
    (.stop osc (+ start-time duration))))

;; Charango: 5 courses. Course 2 (3rd, middle E) has octave pair for richer tone.
(def course-base-freqs #js [329.63 440.00 329.63 261.63 196.00])
(def octave-courses #{2})  ;; 0-indexed: course 2 = 3rd course, middle E

(defn play-course [ctx base-freq fret start-time duration vol has-octave?]
  "Play a single course. If has-octave?, also play one octave down."
  (let [freq (* base-freq (Math/pow 2 (/ fret 12)))]
    (play-string ctx freq start-time duration vol)
    (when has-octave?
      (play-string ctx (/ freq 2) start-time duration (* vol 0.7)))))

(defn play-chord [chord-key]
  (if-let [ctx (get-audio-ctx)]
    (if-let [chord-data (get chords-db chord-key)]
      (let [fingering (get chord-data :fingering)
            now (.-currentTime ctx)
            vol @volume
            strum-delay 0.08]
        (dotimes [i course-count]
          (let [course-data (get fingering i)
                fret (get course-data 1)]
            (when (>= fret 0)
              (let [base-freq (aget course-base-freqs i)
                    start-time (+ now (* i strum-delay))
                    use-octave? (and (get @app-state :octave-mode)
                                     (contains? octave-courses i))]
                (play-course ctx base-freq fret start-time 0.8 vol use-octave?))))))
      (println "Chord not found:" chord-key))
    (println "Audio context not available")))

(defn play-progression [chord-keys]
  (if-let [ctx (get-audio-ctx)]
    (let [now (.-currentTime ctx)
          chord-duration 1.2
          strum-delay 0.08
          vol @volume]
      (dotimes [ci (count chord-keys)]
        (let [chord-key (get chord-keys ci)
              chord-data (get chords-db chord-key)
              chord-start (+ now (* ci chord-duration))]
          (when chord-data
            (let [fingering (get chord-data :fingering)]
              (dotimes [si course-count]
                (let [course-data (get fingering si)
                      fret (get course-data 1)]
                  (when (>= fret 0)
                    (let [base-freq (aget course-base-freqs si)
                          start-time (+ chord-start (* si strum-delay))
                          use-octave? (and (get @app-state :octave-mode)
                                           (contains? octave-courses si))]
                      (play-course ctx base-freq fret start-time 0.8 vol use-octave?))))))))))
    (println "Audio context not available")))

;; ── App State ─────────────────────────────────────────────────────
(def app-state
  (atom {:active-tab "chords"
         :search ""
         :filter-root "All"
         :filter-type "All"
         :filter-difficulty "All"
         :selected-chord nil
         :favorites (let [stored (.getItem js/localStorage "charango-favorites")]
                      (if stored
                        (into #{} (.parse js/JSON stored))
                        #{}))
         :dark-theme false
         :transpose 0
         :volume 0.5
         :octave-mode true}))  ;; true = authentic octave separation on course 3

;; ── Chord Progressions ────────────────────────────────────────────
(def progressions
  [{:name "I-IV-V (Huayno)"     :genre "Andean"    :key "C"
    :chords ["C" "F" "G"]
    :description "Traditional huayno progression — the heartbeat of Andean music."}
   {:name "i-VII-VI-V (Andean)" :genre "Andean"    :key "C"
    :chords ["Am" "G" "F" "E"]
    :description "Descending Andean cadence. Dramatic and beautiful."}
   {:name "I-V-vi-IV (Latin)"   :genre "Latin Pop" :key "C"
    :chords ["C" "G" "Am" "F"]
    :description "The universal Latin pop progression."}
   {:name "vi-IV-I-V (Cueca)"   :genre "Cueca"     :key "C"
    :chords ["Am" "F" "C" "G"]
    :description "Classic cueca chilena — national dance of Chile."}
   {:name "ii-V-I (Jazz)"       :genre "Jazz"      :key "C"
    :chords ["Dm" "G7" "C"]
    :description "Essential jazz turnaround for charango jazz fusion."}
   {:name "I-IV-I-V (Folk)"     :genre "Folk"      :key "C"
    :chords ["C" "F" "C" "G7"]
    :description "Simple folk progression, common in charango accompaniment."}
   {:name "i-iv-V7 (Carnaval)"  :genre "Carnavalito" :key "C"
    :chords ["Cm" "Fm" "G7"]
    :description "Carnavalito rhythm — festive Andean celebration music."}
   {:name "I-V-IV-I (Rock)"     :genre "Rock"      :key "C"
    :chords ["C" "G" "F" "C"]
    :description "Rock progression adapted for charango."}
   {:name "i7-IV7 (Funk)"       :genre "Funk"      :key "C"
    :chords ["Cm7" "F7" "Cm7" "F7"]
    :description "Funky charango groove."}
   {:name "vi-I-IV-V (Pop)"     :genre "Pop"       :key "C"
    :chords ["Am" "C" "F" "G"]
    :description "Pop progression, works beautifully on charango."}
   {:name "I-V-I-IV (Zamba)"    :genre "Zamba"     :key "C"
    :chords ["C" "G" "C" "F"]
    :description "Zamba argentina — slow, elegant dance rhythm."}
   {:name "i-bVII-bVI-V (Flam)" :genre "Flamenco"  :key "C"
    :chords ["Cm" "Bb" "F" "G7"]
    :description "Andalusian cadence — works on charango with dramatic effect."}])

(defn get-transposed-progression [prog semitones]
  (let [orig-chords (get prog :chords)
        new-chords (mapv #(chord-name-transpose % semitones) orig-chords)
        new-key (chord-name-transpose (get prog :key) semitones)]
    (assoc (assoc prog :chords new-chords) :key new-key)))

;; ── Helpers ───────────────────────────────────────────────────────
(def chord-types ["major" "minor" "dominant7" "maj7" "minor7" "sus2" "sus4"
                  "dim" "dim7" "aug" "m7b5" "add9" "9th" "maj9" "m9"
                  "6th" "m6" "7#9" "7b9" "13th"])
(def difficulty-levels ["beginner" "intermediate" "advanced"])

(defn filter-chords [state]
  (let [search (.toLowerCase (get state :search))
        filter-root (get state :filter-root)
        filter-type (get state :filter-type)
        filter-difficulty (get state :filter-difficulty)]
    (filterv
      (fn [k]
        (let [cd (get chords-db k)]
          (and cd
               (or (= search "") (>= (.indexOf (.toLowerCase k) search) 0))
               (or (= filter-root "All") (= (get cd :root) filter-root))
               (or (= filter-type "All") (= (get cd :type) filter-type))
               (or (= filter-difficulty "All") (= (get cd :difficulty) filter-difficulty)))))
      (keys chords-db))))

;; ── UI Components ─────────────────────────────────────────────────
(defn difficulty-tag [level]
  (let [color (case level
                "beginner" "is-success"
                "intermediate" "is-warning"
                "advanced" "is-danger"
                "is-info")]
    [:span {:class (str "tag " color " is-light")} level]))

(defn toggle-favorite [chord-key]
  (let [curr @app-state
        favs (get curr :favorites)]
    (swap! app-state assoc :favorites
           (if (contains? favs chord-key)
             (disj favs chord-key)
             (conj favs chord-key)))
    (js/setTimeout (fn []
      (let [new-favs (get @app-state :favorites)]
        (.setItem js/localStorage "charango-favorites"
                  (.stringify js/JSON (into-array new-favs)))))
      0)))

(defn favorite-star [chord-key favorites]
  (let [is-fav (contains? favorites chord-key)]
    [:button {:class (str "button is-small " (if is-fav "is-warning" "is-light"))
              :onclick (fn [e]
                         (.stopPropagation e)
                         (toggle-favorite chord-key))}
     (if is-fav "★" "☆")]))

(defn chord-card [chord-key chord-data state]
  (let [favorites (get state :favorites)
        is-fav (contains? favorites chord-key)]
    [:div {:class "column is-one-quarter-desktop is-half-tablet is-full-mobile"}
     [:div {:class (str "box has-text-centered p-2 chord-card "
                         (if is-fav "has-background-warning-light" ""))
            :style "cursor:pointer;transition:transform 0.2s;"
            :onclick (fn [_] (swap! app-state assoc :selected-chord chord-key))
            :onmouseenter (fn [e] (set! (.. (.-currentTarget e) -style -transform) "scale(1.03)"))
            :onmouseleave (fn [e] (set! (.. (.-currentTarget e) -style -transform) "scale(1)"))}
      [:p {:class "has-text-weight-bold is-size-5 mb-1"} chord-key]
      [:div {:class "mb-2"} (difficulty-tag (get chord-data :difficulty))]
      (render-chord-svg chord-data 120 160)
      [:div {:class "field has-addons is-justify-content-center mt-2"}
       [:p {:class "control"}
        [:button {:class "button is-small is-info"
                  :onclick (fn [e] (.stopPropagation e) (play-chord chord-key))}
         "▶ Play"]]
       [:p {:class "control"} (favorite-star chord-key favorites)]]]]))

(defn chord-modal []
  (let [state @app-state
        chord-key (get state :selected-chord)]
    (when chord-key
      (let [chord-data (get chords-db chord-key)
            fingering (get chord-data :fingering)
            favorites (get state :favorites)]
        [:div {:class "modal is-active"
               :onclick (fn [_] (swap! app-state assoc :selected-chord nil))}
         [:div {:class "modal-background"}]
         [:div {:class "modal-card" :style "max-width:500px;"
                :onclick (fn [e] (.stopPropagation e))}
          [:header {:class "modal-card-head"}
           [:p {:class "modal-card-title"} (get chord-data :name)]
           [:button {:class "delete" :aria-label "close"
                     :onclick (fn [_] (swap! app-state assoc :selected-chord nil))}]]
          [:section {:class "modal-card-body"}
           [:div {:class "has-text-centered"}
            (render-chord-svg chord-data 200 260)]
           [:div {:class "content mt-3"}
            [:p [:strong "Root: "] (get chord-data :root)]
            [:p [:strong "Type: "] (get chord-data :type)]
            [:p [:strong "Intervals: "] (str (get chord-data :intervals))]
            [:p [:strong "Difficulty: "] (difficulty-tag (get chord-data :difficulty))]
            (into [:table {:class "table is-striped is-narrow is-fullwidth"}]
                  (into [:thead [:tr [:th "Course"] [:th "Note"] [:th "Fret"] [:th "Finger"]]]
                        [(into [:tbody]
                               (mapv (fn [i]
                                       (let [s (get fingering i)
                                             fret (get s 1)
                                             finger (get s 2)]
                                         [:tr {:key (str "fret-" i)}
                                          [:td (get course-names i)]
                                          [:td (if (>= fret 0) (fret->note-solver i fret) "-")]
                                          [:td (if (= fret -1) [:span {:style "color:#e74c3c"} "Muted"]
                                                   (if (= fret 0) [:span {:style "color:#27ae60"} "Open"] fret))]
                                          [:td (if (> finger 0) finger "-")]]))
                                     (range course-count)))]))
            [:div {:class "has-text-centered mt-3"}
             [:button {:class "button is-info is-medium"
                       :onclick (fn [_] (play-chord chord-key))}
              "▶ Strum Chord"]]]]
          [:footer {:class "modal-card-foot"}
           (favorite-star chord-key favorites)
           [:button {:class "button"
                     :onclick (fn [_] (swap! app-state assoc :selected-chord nil))}
            "Close"]]]]))))

(defn progression-card [prog transpose-amt]
  (let [tp (if (= transpose-amt 0) prog (get-transposed-progression prog transpose-amt))
        chords (get tp :chords)
        key-display (get tp :key)
        chord-els (mapv (fn [c]
                          (let [cd (get chords-db c)]
                            [:div {:class "has-text-centered mx-2" :key c}
                             (if cd
                               [:div
                                (render-chord-svg cd 110 145)
                                [:p {:class "is-size-7 has-text-weight-bold mt-1"} c]]
                               [:div {:class "box p-2"} [:p {:class "is-size-7"} c]])]))
                        chords)]
    [:div {:class "box progression-card mb-4"}
     [:div {:class "level is-mobile mb-2"}
      [:div {:class "level-left"}
       [:div {:class "level-item"}
        [:span {:class (str "tag is-medium " (case (get tp :genre)
                                         "Andean" "is-success"
                                         "Cueca" "is-danger"
                                         "Carnavalito" "is-warning"
                                         "Zamba" "is-link"
                                         "Latin Pop" "is-info"
                                         "Flamenco" "is-dark"
                                         "is-primary"))}
         (get tp :genre)]]
       [:div {:class "level-item"}
        [:span {:class "has-text-weight-bold is-size-5"} (get tp :name)]]
       (when (not= transpose-amt 0)
         [:div {:class "level-item"}
          [:span {:class "tag is-warning"} "Key: " key-display]])]
      [:div {:class "level-right"}
       [:div {:class "level-item"}
        [:button {:class "button is-info is-small"
                  :onclick (fn [_] (play-progression chords))}
         "▶ Play"]]]]
     [:p {:class "is-size-7 has-text-grey mb-3"} (get tp :description)]
     (into [:div {:class "is-flex is-justify-content-center is-align-items-center"
                  :style "overflow-x:auto;padding:0.5rem 0;"}]
           chord-els)]))

;; ── Main Views ────────────────────────────────────────────────────
(defn chords-view [state]
  (let [filtered (filter-chords state)]
    [:div
     [:div {:class "field is-horizontal is-flex-wrap-wrap"}
      [:div {:class "field-body mr-2"}
       [:p {:class "control has-icons-left"}
        [:input {:class "input" :type "text" :placeholder "Search chords (e.g. Am7)..."
                 :value (get state :search)
                 :oninput (fn [e] (swap! app-state assoc :search (.. e -target -value)))}]
        [:span {:class "icon is-left"} "🎵"]]]
      [:div {:class "field mr-2"}
       [:div {:class "select"}
        (into [:select {:value (get state :filter-root)
                        :onchange (fn [e] (swap! app-state assoc :filter-root (.. e -target -value)))}
               [:option {:value "All"} "All Roots"]]
              (mapv (fn [n] [:option {:value n :key n} n]) all-notes))]]
      [:div {:class "field mr-2"}
       [:div {:class "select"}
        (into [:select {:value (get state :filter-type)
                        :onchange (fn [e] (swap! app-state assoc :filter-type (.. e -target -value)))}
               [:option {:value "All"} "All Types"]]
              (mapv (fn [t] [:option {:value t :key t} t]) chord-types))]]
      [:div {:class "field"}
       [:div {:class "select"}
        (into [:select {:value (get state :filter-difficulty)
                        :onchange (fn [e] (swap! app-state assoc :filter-difficulty (.. e -target -value)))}
               [:option {:value "All"} "All Levels"]]
              (mapv (fn [d] [:option {:value d :key d} d]) difficulty-levels))]]]
     [:p {:class "has-text-grey is-size-7 mb-3"}
      (count filtered) " chord" (if (not= (count filtered) 1) "s" "") " found"]
     (into [:div {:class "columns is-multiline is-mobile"}]
           (mapv (fn [k] (chord-card k (get chords-db k) state)) filtered))
     (when (zero? (count filtered))
       [:div {:class "has-text-centered has-text-grey py-6"}
        [:p {:class "is-size-4"} "🦙"]
        [:p "No charango chords match. Try adjusting your search."]])]))

(defn progressions-view [state]
  (let [transpose-amt (get state :transpose)
        ;; Group progressions by genre
        by-genre (reduce (fn [acc p]
                           (let [genre (get p :genre)]
                             (assoc acc genre (conj (get acc genre []) p))))
                         {}
                         progressions)
        genre-order ["Andean" "Cueca" "Carnavalito" "Zamba" "Latin Pop" "Jazz" "Folk" "Rock" "Funk" "Flamenco"]]
    [:div
     ;; Transpose control
     [:div {:class "field is-horizontal is-align-items-center mb-4"}
      [:div {:class "field-label is-normal"}
       [:label {:class "label"} "Transpose:"]]
      [:div {:class "field-body"}
       [:div {:class "field has-addons"}
        [:p {:class "control"}
         [:button {:class "button"
                   :onclick (fn [_] (swap! app-state update :transpose #(max -6 (dec %))))}
          "−"]]
        [:p {:class "control"}
         [:input {:class "input has-text-centered" :type "text" :style "width:60px;"
                  :value (str (if (> transpose-amt 0) "+" "") transpose-amt) :readonly true}]]
        [:p {:class "control"}
         [:button {:class "button"
                   :onclick (fn [_] (swap! app-state update :transpose #(min 6 (inc %))))}
          "+"]]
        [:p {:class "control ml-2"}
         [:button {:class "button is-small"
                   :onclick (fn [_] (swap! app-state assoc :transpose 0))}
          "Reset"]]]]]
     ;; Progressions grouped by genre
     (into [:div]
           (mapv (fn [genre]
                   (when-let [progs (get by-genre genre)]
                     [:div {:class "mb-5" :key genre}
                      [:h3 {:class "title is-5 mb-3"}
                       [:span {:class (str "tag is-medium " (case genre
                                                        "Andean" "is-success"
                                                        "Cueca" "is-danger"
                                                        "Carnavalito" "is-warning"
                                                        "Zamba" "is-link"
                                                        "Latin Pop" "is-info"
                                                        "Flamenco" "is-dark"
                                                        "is-primary"))}
                        genre]
                       " " (count progs) " progression" (if (> (count progs) 1) "s" "")]
                      (into [:div]
                            (mapv (fn [p]
                                    (progression-card p transpose-amt))
                                  progs))]))
                 genre-order))]))

(defn favorites-view [state]
  (let [favs (get state :favorites)]
    (if (empty? favs)
      [:div {:class "has-text-centered has-text-grey py-6"}
       [:p {:class "is-size-3"} "⭐"]
       [:p {:class "is-size-5"} "No favorites yet"]
       [:p "Click the star on any chord to add it here."]]
      [:div
       [:p {:class "has-text-grey is-size-7 mb-3"}
        (count favs) " favorite chord" (if (not= (count favs) 1) "s" "")]
       (into [:div {:class "columns is-multiline is-mobile"}]
             (mapv (fn [k]
                     (when-let [cd (get chords-db k)]
                       (chord-card k cd state)))
                   (sort (vec favs))))])))

(defn navbar [state]
  (let [active (get state :active-tab)
        dark (get state :dark-theme)]
    [:nav {:class (str "navbar " (if dark "is-dark" "is-light")) :role "navigation"}
     [:div {:class "navbar-brand"}
      [:a {:class "navbar-item"
           :onclick (fn [_] (swap! app-state assoc :active-tab "chords"))}
       [:span {:class "is-size-4 mr-2"} "🦙"]
       [:span {:class "has-text-weight-bold is-size-5"} "Charango Chords Pro"]]
      [:a {:class "navbar-burger" :role "button" :aria-label "menu" :aria-expanded "false"
           :onclick (fn [e]
                      (let [target (.querySelector js/document ".navbar-menu")
                            burger (.querySelector js/document ".navbar-burger")]
                        (when (and target burger)
                          (.toggle (.-classList target) "is-active")
                          (.toggle (.-classList burger) "is-active"))))}
       [:span {:aria-hidden "true"}] [:span {:aria-hidden "true"}] [:span {:aria-hidden "true"}]]]
     [:div {:class "navbar-menu"}
      [:div {:class "navbar-start"}
       [:a {:class (str "navbar-item " (if (= active "chords") "is-active" ""))
            :onclick (fn [_] (swap! app-state assoc :active-tab "chords"))}
        "Chords"]
       [:a {:class (str "navbar-item " (if (= active "progressions") "is-active" ""))
            :onclick (fn [_] (swap! app-state assoc :active-tab "progressions"))}
        "Progressions"]
       [:a {:class (str "navbar-item " (if (= active "favorites") "is-active" ""))
            :onclick (fn [_] (swap! app-state assoc :active-tab "favorites"))}
        (str "Favorites (" (count (get state :favorites)) ")")]]
      [:div {:class "navbar-end"}
       ;; Octave mode toggle
       [:div {:class "navbar-item"}
        [:button {:class (str "button is-small " (if (get state :octave-mode) "is-success" "is-light"))
                  :title (if (get state :octave-mode) "Octave separation ON — authentic charango sound" "Unison mode — both strings same pitch")
                  :onclick (fn [_] (swap! app-state update :octave-mode not))}
         (if (get state :octave-mode) "🎵 Octave" "🎵 Unison")]]
       ;; Volume slider
       [:div {:class "navbar-item"}
        [:input {:class "slider" :type "range" :min "0" :max "1" :step "0.05"
                 :value (get state :volume) :style "width:80px;"
                 :oninput (fn [e]
                            (let [v (js/parseFloat (.. e -target -value))]
                              (swap! app-state assoc :volume v)
                              (reset! volume v)))}]
        [:span {:class "tag is-info ml-2 is-size-7"}
         (str "🔊 " (int (* (get state :volume) 100)) "%")]]
       ;; Theme toggle
       [:div {:class "navbar-item"}
        [:button {:class (str "button is-small " (if dark "is-warning" "is-dark"))
                  :onclick (fn [_] (swap! app-state update :dark-theme not))}
         (if dark "☀ Light" "🌙 Dark")]]]]]))

(defn app-ui [state]
  (let [active (get state :active-tab)
        dark (get state :dark-theme)]
    [:div {:class (if dark "has-background-dark has-text-light" "")}
     (navbar state)
     [:section {:class "section py-3"}
      (case active
        "chords" (chords-view state)
        "progressions" (progressions-view state)
        "favorites" (favorites-view state)
        (chords-view state))]
     (chord-modal)
     [:footer {:class "footer py-3 has-text-centered"}
      [:p {:class "is-size-7 has-text-grey"}
       "Charango Chords Pro · " (count (keys chords-db)) " chords · "
       (count progressions) " progressions · Built with Squint + Reagami + Bulma"]]
     [:style "
       .chord-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
       .progression-card { transition: box-shadow 0.2s; }
       .progression-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
       html { scroll-behavior: smooth; }
     "]]))

;; ── Render ────────────────────────────────────────────────────────
(defn render []
  (.render rg (js/document.getElementById "app") (app-ui @app-state)))

(render)

(js/setInterval
  (fn []
    (.render rg (js/document.getElementById "app") (app-ui @app-state)))
  250)
