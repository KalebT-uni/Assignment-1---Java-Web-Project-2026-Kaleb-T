# Speech-to-Text Web Application

A Java Spring Boot web application for COMP3011 (Adelaide University),
Assignment 1. Records audio from the browser's microphone, transcribes it
via a Cloud speech-to-text API, and displays the result. Also exposes
administration and statistics endpoints per the assignment's YAML API
specification.

## Running locally

1. Build the executable JAR:

2. Run it:

3. Open http://localhost:8080/ in a browser.

## Configuration

The application reads two environment variables:

- `OPENAI_API_KEY` — bearer token for the real OpenAI transcription API.
  Provided automatically on TITAN; not required for local testing against
  the local adapter below.
- `OPENAI_API_URL` — the transcription endpoint to call. Defaults to
  OpenAI's real endpoint if unset.

## Local testing without an OpenAI key

Rather than pay for OpenAI API credits during development, this project
was tested locally against a self-hosted [whisper.cpp](https://github.com/ggml-org/whisper.cpp)
adapter that mimics OpenAI's response shape
(https://github.com/scottmac-dev/whisper-cpp-adapter). To use it:

1. Run the adapter locally (see its own README for setup), which serves
   on `http://localhost:8000` by default.
2. Set `OPENAI_API_URL=http://localhost:8000/v1/audio/transcriptions`
   before running this application.

No code changes are needed to switch between the local adapter and the
real OpenAI API — only the environment variables differ.

## API endpoints

See the assignment's YAML specification for the full contract of
`/api/v1/admin/uptime`, `/api/v1/admin/shutdown`, and
`/api/v1/global/stats`. The transcription endpoint is
`POST /api/v1/transcribe`, accepting a multipart form with an `audio` field.


## Testing

`TranscriptionControllerTest` provides regression tests for the
transcription endpoint using a mocked `TranscriptionService`, covering
both a successful transcription and the structured error response
returned when the STT service fails — without needing a real network
call or Cloud STT service.