# SolidWorks Pipe Worker

This local HTTP worker is used by the RuoYi design platform to generate the
pipe CAD outputs from five design variables.

## Start

Double-click:

```text
start_pipe_worker.bat
```

It listens on:

```text
http://127.0.0.1:18080/api/pipe-model
```

## Behavior

The worker first generates the pipe path files, then requires SolidWorks COM
automation to create the native `SLDPRT` model. The provided
`start_pipe_worker.bat` sets `PIPE_WORKER_RUN_SOLIDWORKS=1` by default.

If SolidWorks does not generate `pipe_native.SLDPRT`, the worker returns a
failure instead of treating the STL preview as a successful CAD result. Set
`PIPE_WORKER_RUN_SOLIDWORKS=0` only for temporary geometry-debug mode.
