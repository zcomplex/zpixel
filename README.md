# Z-Pixel (draft)

ZPixel is a research project that represents also an abstraction for the virtual holographic pixel that allows to see
more and beyond.

## Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will
start a Scala 3 REPL.

## Workflow
The following commands help us to record from camera, prepare frames for processing and build the final video.

### 1. Recording
With the following command we can record from the specified video input device:

`ffmpeg -f v4l2 -framerate 30 -t 20 -video_size 640x480 -i /dev/video0 -c:v rawvideo -pix_fmt yuv422p rec.avi`

### 2. Frames extraction
Let's use the following command to extract frames from the recorded video:

`ffmpeg -i rec.avi -vsync 0 -frame_pts 1 frames/f_%06d.png`

### 3. Z-Processing
In this phase we can run the program to process all extracted frames and produce
z-frames for next step.

### 4. Making video
To make the final video we can run the following command that builds out z-video
from just created z-frames:

`ffmpeg -framerate 30 -i zf_%04d.png -c:v rawvideo -pix_fmt rgb24 zvideo.avi`