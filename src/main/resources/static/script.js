let mediaRecorder;
let audioChunks = [];

const recordButton = document.getElementById('recordButton');
const statusText = document.getElementById('status');
const transcriptText = document.getElementById('transcript');

function setStatus(message, isError) {
    statusText.textContent = message;
    statusText.classList.toggle('error', Boolean(isError));
}

async function startRecording() {
    let stream;
    try {
        stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch (error) {
        // Distinguish a denied/unavailable microphone from any other failure,
        // since this is the most common real-world error users will hit.
        if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
            setStatus('Microphone access was denied. Please allow microphone access and try again.', true);
        } else if (error.name === 'NotFoundError') {
            setStatus('No microphone was found on this device.', true);
        } else {
            setStatus('Could not access the microphone: ' + error.message, true);
        }
        return;
    }

    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];

    mediaRecorder.ondataavailable = (event) => {
        audioChunks.push(event.data);
    };

    mediaRecorder.onstop = async () => {
        setStatus('Transcribing...', false);
        recordButton.textContent = 'Start Recording';
        recordButton.classList.remove('recording');
        recordButton.disabled = true;

        const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
        const formData = new FormData();
        formData.append('audio', audioBlob, 'recording.webm');

        try {
            const response = await fetch('/api/v1/transcribe', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                const errorBody = await response.json().catch(() => null);
                const detail = errorBody && errorBody.message ? errorBody.message : 'The server returned an error.';
                throw new Error(detail);
            }

            const data = await response.json();
            transcriptText.textContent = data.text;
            transcriptText.classList.add('visible');
            setStatus('', false);
        } catch (error) {
            setStatus('Could not transcribe audio: ' + error.message, true);
        } finally {
            recordButton.disabled = false;
        }

        stream.getTracks().forEach(track => track.stop());
    };

    mediaRecorder.start();
    recordButton.textContent = 'Stop Recording';
    recordButton.classList.add('recording');
    setStatus('Recording...', false);
    transcriptText.textContent = '';
    transcriptText.classList.remove('visible');
}

recordButton.addEventListener('click', async () => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        mediaRecorder.stop();
        return;
    }
    await startRecording();
});