# 🦙 Charanguito — Chords & Tuner

A bilingual (English/Español) charango chords library and tuner, built with Squint ClojureScript, Reagami, and Bulma.

**[Live Demo →](https://mrsipan.github.io/charanguito/)**

## Features

- **340+ chords** — 19 hand-curated + auto-generated via fretboard solver
- **Interactive SVG diagrams** — double lines show the paired strings of each course
- **12 Andean/Latin progressions** — Huayno, Cueca, Carnavalito, Zamba, and more
- **Built-in tuner** — autocorrelation pitch detection with cents deviation display
- **Audio playback** — Web Audio API strumming with octave pair on the 3rd course
- **Bilingual** — toggle between English (C, D, E) and Spanish (Do, Re, Mi)
- **Dark mode, favorites, transpose, volume control**

## Charango Tuning

The charango has 10 strings in 5 courses (pairs), tuned:

| Course | Notes | Frequency |
|---|---|---|
| 1st (highest) | E — E | 329.63 Hz |
| 2nd | A — A | 440.00 Hz |
| 3rd (middle) | E — e (octave) | 329.63 / 164.81 Hz |
| 4th | C — C | 261.63 Hz |
| 5th (lowest) | G — G | 196.00 Hz |

## Tech Stack

- [Squint](https://github.com/squint-cljs/squint) — ClojureScript compiler
- [Reagami](https://github.com/squint-cljs/reagami) — Hiccup UI library
- [Bulma](https://bulma.io/) — CSS framework
- Web Audio API — sound and pitch detection

## Run Locally

```bash
python3 -m http.server 8000
# Open http://localhost:8000
```

No build step needed — the standalone HTML embeds the Squint compiler.

## License

MIT
