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

Dim swApp, swModel, swFeat, swPathSketch, swPathFeat, ok, partTemplate, outPart
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
swModel.SketchManager.CreateLine -0.0792393326, 0.0110058246, 0, 0.2773376640, -0.0385203860, 0
swModel.SketchManager.Create3PointArc 0.2773376640, -0.0385203860, 0, 0.2941424153, -0.0541403802, 0, 0.2882025223, -0.0436811242, 0
swModel.SketchManager.CreateLine 0.2941424153, -0.0541403802, 0, 0.3255662073, -0.2008119360, 0
swModel.SketchManager.Create3PointArc 0.3255662073, -0.2008119360, 0, 0.3389728180, -0.2156531890, 0, 0.3302811618, -0.2100287077, 0
swModel.SketchManager.CreateLine 0.3389728180, -0.2156531890, 0, 0.6761243677, -0.3245983871, 0
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

Set swFeat = swModel.FeatureManager.InsertProtrusionSwept4(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, True, 0, True, True, 0.0300000000, 0)
If swFeat Is Nothing Then
  MsgBox "The 3D path was created, but the automatic sweep failed. Please check that pipe_outer_diameter_mm is smaller than the bend radius, then run Sweep Boss/Base using circular profile.", vbExclamation, "Native pipe model"
  WScript.Quit 0
End If

swModel.ClearSelection2 True
Set swRightBasePlaneFeat = FindNthFeatureByType(swModel, "RefPlane", 3)
If swRightBasePlaneFeat Is Nothing Then
  MsgBox "The pipe was created, but the right reference plane could not be found for wall cutting.", vbExclamation, "Native pipe model"
Else
  swRightBasePlaneFeat.Name = "PipeLeftWallPlane"
  Set swLeftCutFeat = CutWithPlane(swModel, swRightBasePlaneFeat, "PipeLeftWallTrim", False)
  swModel.ClearSelection2 True
  ok = swRightBasePlaneFeat.Select2(False, 0)
  If ok Then
    Set swRefPlane = swModel.FeatureManager.InsertRefPlane(8, 0.6000000000, 0, 0, 0, 0)
    Set swRightWallPlaneFeat = FindLastFeatureByType(swModel, "RefPlane")
    If Not swRightWallPlaneFeat Is Nothing Then swRightWallPlaneFeat.Name = "PipeRightWallPlane"
  End If
  If Not swRightWallPlaneFeat Is Nothing Then
    Set swRightCutFeat = CutWithPlane(swModel, swRightWallPlaneFeat, "PipeRightWallTrim", True)
  End If
  If swLeftCutFeat Is Nothing Or swRightCutFeat Is Nothing Then
    MsgBox "The pipe was created, but one surface trim failed. The model may still contain an extended end; please report this message so the trim direction can be adjusted for your SolidWorks version.", vbExclamation, "Native pipe model"
  End If
End If

outPart = "C:\\Users\\wrr\\IdeaProjects\\zlzs211-clean\\solidworks_pipe\\pipe_native.SLDPRT"
swModel.SaveAs outPart
swModel.ViewZoomtofit2
MsgBox "Native SolidWorks pipe model created." & vbCrLf & outPart & vbCrLf & "L3 = 274.317 mm, initial angle = -7.907 deg", vbInformation, "Native pipe model"
