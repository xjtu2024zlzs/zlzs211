Option Explicit

Dim shell, fso, baseDir, py, scriptPath, macroPath, partPath, cmd
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

baseDir = fso.GetParentFolderName(WScript.ScriptFullName)
py = "python"
scriptPath = fso.BuildPath(baseDir, "generate_pipe.py")
macroPath = fso.BuildPath(baseDir, "create_pipe_native.vbs")
partPath = fso.BuildPath(baseDir, "pipe_native.SLDPRT")

cmd = "cmd /c cd /d """ & baseDir & """ && " & py & " """ & scriptPath & """"
shell.Run cmd, 1, True

If Not fso.FileExists(macroPath) Then
  MsgBox "SolidWorks native macro was not generated: " & macroPath, vbExclamation, "Pipe model"
  WScript.Quit 1
End If

shell.Run "wscript """ & macroPath & """", 1, True

If fso.FileExists(partPath) Then
  MsgBox "Native SolidWorks pipe model generated:" & vbCrLf & partPath, vbInformation, "Pipe model"
End If
