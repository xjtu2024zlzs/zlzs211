# Parametric SolidWorks Pipe Generator

This folder generates the pipe shown in the reference image from five design
variables: `L1`, `L2`, `R`, `theta1`, and `theta2`.

## How to Use

1. Edit `params.json`.
2. Double-click `open_in_solidworks.vbs`.
3. SolidWorks creates and saves `pipe_native.SLDPRT`.

The script also writes `pipe_centerline.csv`, including the computed `L3`.
`pipe_model.stl` is still generated only as a visual fallback. Use
`pipe_native.SLDPRT` or the exported neutral CAD files from the worker for
SolidWorks modeling, recognition, and simulation.

## Angle Convention

Angles are signed tangent deflection angles in degrees:

- Positive angle: counterclockwise turn.
- Negative angle: clockwise turn.

For the shape in the image, `theta1` is usually negative and `theta2` is
usually positive. If your sketch dimensions are the supplementary displayed
bend angles, use:

```text
deflection = 180 - displayed_angle
```

then apply the sign above.

## Fixed Endpoints

The generated centerline starts at `(0, 0, 0)` and ends at
`(600, -300, 0)` in millimeters. This represents 600 mm horizontal length and
300 mm downward vertical height.

## SolidWorks Output

`generate_pipe.py` writes `create_pipe_native.vbs`, which opens SolidWorks,
creates a new part, builds a 3D sketch from straight segments and exact
three-point bend arcs, and the solved final segment `L3`, then creates the
outer circular-profile sweep and hollows it by shelling or by a swept inner
bore cut.

The bend arcs are not created with SolidWorks' ambiguous arc-direction flag.
Their start point, end point, and mid-arc point are calculated first, so the
model follows the intended small tangent arc instead of the opposite large arc.

The pipe section uses both `pipe_outer_diameter_mm` and
`pipe_inner_diameter_mm`. If SolidWorks cannot create the hollow bore, the
worker must treat the model as failed instead of sending a solid pipe to ANSYS.
