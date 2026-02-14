import sys
from faster_whisper import WhisperModel

def main():
    if len(sys.argv) < 2:
        print("No audio file provided")
        sys.exit(1)

    audio_path = sys.argv[1]

    # Load once per run (okay). For max speed, make it a server later.
    model = WhisperModel("base", device="cpu", compute_type="int8")

    segments, info = model.transcribe(audio_path, beam_size=5)  # auto language detect
    text = " ".join(seg.text.strip() for seg in segments).strip()

    print(text)

if __name__ == "__main__":
    main()
