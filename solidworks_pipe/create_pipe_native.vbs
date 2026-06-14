Option Explicit

Function FindLast3DSketchFeature(model)
  Dim feat, last3dSketch, typeName
  Set last3dSketch = Nothing
  Set feat = model.FirstFeature
  Do While Not feat Is Nothing
    typeName = feat.GetTypeName2
    If typeName = "3DProfileFeature" Then
      Set last3dSketch = feat
    End If
    Set feat = feat.GetNextFeature
  Loop
  Set FindLast3DSketchFeature = last3dSketch
End Function

Function FindNthFeatureByType(model, wantedType, wantedIndex)
  Dim feat, count
  count = 0
  Set feat = model.FirstFeature
  Do While Not feat Is Nothing
    If feat.GetTypeName2 = wantedType Then
      count = count + 1
      If count = wantedIndex Then
        Set FindNthFeatureByType = feat
        Exit Function
      End If
    End If
    Set feat = feat.GetNextFeature
  Loop
  Set FindNthFeatureByType = Nothing
End Function

Function FindLastFeatureByType(model, wantedType)
  Dim feat, lastFeat
  Set lastFeat = Nothing
  Set feat = model.FirstFeature
  Do While Not feat Is Nothing
    If feat.GetTypeName2 = wantedType Then
      Set lastFeat = feat
    End If
    Set feat = feat.GetNextFeature
  Loop
  Set FindLastFeatureByType = lastFeat
End Function

Function SelectPipeEndFacesForShell(model, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)
  Dim ok1, ok2
  model.ClearSelection2 True
  ok1 = model.Extension.SelectByRay(sx - stx * searchRadius, sy - sty * searchRadius, sz - stz * searchRadius, stx, sty, stz, searchRadius, 2, False, 0, 0)
  ok2 = model.Extension.SelectByRay(ex + etx * searchRadius, ey + ety * searchRadius, ez + etz * searchRadius, -etx, -ety, -etz, searchRadius, 2, True, 0, 0)
  SelectPipeEndFacesForShell = ok1 And ok2
End Function

Function ApplyPipeShell(model, thickness, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)
  Dim ok, shellFeat
  Set shellFeat = Nothing
  ok = SelectPipeEndFacesForShell(model, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)
  If Not ok Then
    Set ApplyPipeShell = Nothing
    Exit Function
  End If
  On Error Resume Next
  Set shellFeat = model.FeatureManager.InsertShell(thickness, False)
  If shellFeat Is Nothing Then
    Err.Clear
    model.ClearSelection2 True
    ok = SelectPipeEndFacesForShell(model, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)
    If ok Then Set shellFeat = model.FeatureManager.InsertShell(thickness, True)
  End If
  If Err.Number <> 0 Then Err.Clear
  On Error GoTo 0
  If Not shellFeat Is Nothing Then shellFeat.Name = "PipeHollowShell"
  Set ApplyPipeShell = shellFeat
End Function

Function ApplySweptInnerCut(model, pathFeat, innerDiameter)
  Dim ok, cutFeat
  Set cutFeat = Nothing
  model.ClearSelection2 True
  ok = False
  If Not pathFeat Is Nothing Then ok = pathFeat.Select2(False, 4)
  If Not ok Then ok = model.Extension.SelectByID2("InnerBorePath3D", "SKETCH", 0, 0, 0, False, 4, Nothing, 0)
  If Not ok Then
    Set ApplySweptInnerCut = Nothing
    Exit Function
  End If
  On Error Resume Next
  Set cutFeat = model.FeatureManager.InsertCutSwept5(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, 0, True, False, True, True, True, innerDiameter, 0)
  If Err.Number <> 0 Then Err.Clear
  On Error GoTo 0
  If Not cutFeat Is Nothing Then cutFeat.Name = "PipeInnerBoreCut"
  Set ApplySweptInnerCut = cutFeat
End Function

Function CutWithPlane(model, planeFeat, cutName, flipSide)
  Dim ok, cutFeat, errors
  Set cutFeat = Nothing
  errors = 0
  model.ClearSelection2 True
  ok = planeFeat.Select2(False, 0)
  If Not ok Then
    Set CutWithPlane = Nothing
    Exit Function
  End If
  Set cutFeat = model.FeatureManager.InsertCutSurface(flipSide, 0, False, True, Nothing, errors)
  If Not cutFeat Is Nothing Then cutFeat.Name = cutName
  Set CutWithPlane = cutFeat
End Function

Dim swApp, swModel, swFeat, swPathSketch, swPathFeat, swInnerPathSketch, swInnerPathFeat, swShellFeat, swInnerCutFeat, ok, partTemplate, outPart
Dim swStartOpenPathFeat, swEndOpenPathFeat, swStartOpenCutFeat, swEndOpenCutFeat
Dim swRightBasePlaneFeat, swRightWallPlaneFeat, swLeftCutFeat, swRightCutFeat, swRefPlane
Set swApp = CreateObject("SldWorks.Application")
swApp.Visible = True
partTemplate = swApp.GetUserPreferenceStringValue(1)
If Len(partTemplate) > 0 Then
  Set swModel = swApp.NewDocument(partTemplate, 0, 0, 0)
Else
  swApp.NewPart
  Set swModel = swApp.ActiveDoc
End If
If swModel Is Nothing Then
  MsgBox "Cannot create a new SolidWorks part. Please check the default part template.", vbExclamation, "Native pipe model"
  WScript.Quit 1
End If

swModel.SketchManager.Insert3DSketch True
Set swPathSketch = swModel.GetActiveSketch2
swModel.SketchManager.AddToDB = True
swModel.SketchManager.CreateLine 0.0000000000, 0.0000000000, 0, 0.0723299092, -0.0341816360, 0
swModel.SketchManager.Create3PointArc 0.0723299092, -0.0341816360, 0, 0.0831014276, -0.0418067328, 0, 0.0780492232, -0.0375229915, 0
swModel.SketchManager.CreateLine 0.0831014276, -0.0418067328, 0, 0.1393785015, -0.0986652498, 0
swModel.SketchManager.Create3PointArc 0.1393785015, -0.0986652498, 0, 0.1513589425, -0.1068360553, 0, 0.1449753424, -0.1033274452, 0
swModel.SketchManager.CreateLine 0.1513589425, -0.1068360553, 0, 0.6000000000, -0.3000000000, 0
swModel.SketchManager.AddToDB = False
swModel.SketchManager.Insert3DSketch True
Set swPathFeat = FindLast3DSketchFeature(swModel)
If Not swPathFeat Is Nothing Then swPathFeat.Name = "PipePath3D"
swModel.ClearSelection2 True

ok = False
If Not swPathFeat Is Nothing Then ok = swPathFeat.Select2(False, 4)
If Not ok Then ok = swModel.Extension.SelectByID2("PipePath3D", "SKETCH", 0, 0, 0, False, 4, Nothing, 0)
If Not ok Then
  MsgBox "The path sketch was created, but SolidWorks could not select PipePath3D for sweep. Please select it manually and use Sweep Boss/Base with circular profile.", vbExclamation, "Native pipe model"
  WScript.Quit 0
End If

Set swFeat = swModel.FeatureManager.InsertProtrusionSwept4(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, True, 0, True, True, 0.0095300000, 0)
If swFeat Is Nothing Then
  MsgBox "The 3D path was created, but the automatic sweep failed. Please check that pipe_outer_diameter_mm is smaller than the bend radius, then run Sweep Boss/Base using circular profile.", vbExclamation, "Native pipe model"
  WScript.Quit 1
End If

swModel.SketchManager.Insert3DSketch True
Set swInnerPathSketch = swModel.GetActiveSketch2
swModel.SketchManager.AddToDB = True
swModel.SketchManager.CreateLine -0.0723299092, 0.0341816360, 0, 0.0723299092, -0.0341816360, 0
swModel.SketchManager.Create3PointArc 0.0723299092, -0.0341816360, 0, 0.0831014276, -0.0418067328, 0, 0.0780492232, -0.0375229915, 0
swModel.SketchManager.CreateLine 0.0831014276, -0.0418067328, 0, 0.1393785015, -0.0986652498, 0
swModel.SketchManager.Create3PointArc 0.1393785015, -0.0986652498, 0, 0.1513589425, -0.1068360553, 0, 0.1449753424, -0.1033274452, 0
swModel.SketchManager.CreateLine 0.1513589425, -0.1068360553, 0, 0.6734787696, -0.3316365361, 0
swModel.SketchManager.AddToDB = False
swModel.SketchManager.Insert3DSketch True
Set swInnerPathFeat = FindLast3DSketchFeature(swModel)
If Not swInnerPathFeat Is Nothing Then swInnerPathFeat.Name = "InnerBorePath3D"
swModel.ClearSelection2 True

Set swShellFeat = ApplyPipeShell(swModel, 0.0009000000, 0.0000000000, 0.0000000000, 0, 0.9041238645, -0.4272704501, 0, 0.6000000000, -0.3000000000, 0, 0.9184846204, -0.3954567007, 0, 0.0190600000)
If swShellFeat Is Nothing Then
  Set swInnerCutFeat = ApplySweptInnerCut(swModel, swInnerPathFeat, 0.0077300000)
  If Not swInnerCutFeat Is Nothing Then
    swModel.SketchManager.Insert3DSketch True
    swModel.SketchManager.AddToDB = True
    swModel.SketchManager.CreateLine -0.0723299092, 0.0341816360, 0, 0.0516978026, -0.0244313243, 0
    swModel.SketchManager.AddToDB = False
    swModel.SketchManager.Insert3DSketch True
    Set swStartOpenPathFeat = FindLast3DSketchFeature(swModel)
    If Not swStartOpenPathFeat Is Nothing Then swStartOpenPathFeat.Name = "InnerBoreStartOpen3D"
    Set swStartOpenCutFeat = ApplySweptInnerCut(swModel, swStartOpenPathFeat, 0.0077300000)
    swModel.SketchManager.Insert3DSketch True
    swModel.SketchManager.AddToDB = True
    swModel.SketchManager.CreateLine 0.6734787696, -0.3316365361, 0, 0.5474810494, -0.2773877859, 0
    swModel.SketchManager.AddToDB = False
    swModel.SketchManager.Insert3DSketch True
    Set swEndOpenPathFeat = FindLast3DSketchFeature(swModel)
    If Not swEndOpenPathFeat Is Nothing Then swEndOpenPathFeat.Name = "InnerBoreEndOpen3D"
    Set swEndOpenCutFeat = ApplySweptInnerCut(swModel, swEndOpenPathFeat, 0.0077300000)
  End If
End If
If swShellFeat Is Nothing And swInnerCutFeat Is Nothing Then
  MsgBox "The pipe outer sweep succeeded, but SolidWorks could not create the hollow bore. The model was not saved to avoid sending a solid pipe to ANSYS.", vbExclamation, "Native hollow pipe model"
  WScript.Quit 1
End If

outPart = "C:\\Users\\wrr\\IdeaProjects\\zlzs211-clean\\solidworks_pipe\\pipe_native.SLDPRT"
swModel.SaveAs outPart
swModel.ViewZoomtofit2
MsgBox "Native SolidWorks pipe model created." & vbCrLf & outPart & vbCrLf & "L3 = 488.458 mm, initial angle = -25.294 deg, wall = 0.900 mm", vbInformation, "Native pipe model"
