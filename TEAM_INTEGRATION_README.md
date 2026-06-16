## Project2 database history migration

For Project2 master integration, execute database scripts in this order:

1. Back up the target `ry-cloud` database.
2. Execute `sql/t2_project2_master_migration.sql` for schema, menus, roles, and base initialization data.
3. If existing local Project2 task history should be preserved, execute `sql/t2_project2_history_data.sql`.

`sql/t2_project2_history_data.sql` contains historical task records, selected objectives and constraints, design-variable selections, surrogate solve records, CAD records, ANSYS simulation records, approval records, archive records, and task-bound fault-pipe parameter snapshots.

Do not use the history script as a replacement for `sql/t2_project2_master_migration.sql`; it depends on the schema and base initialization created by the master migration script.
## Project2 / Project5 鏈湴鑱旇皟鍓嶇浠ｇ悊

`ruoyi-ui/vite.config.js` 鐨勫紑鍙戠幆澧?`/dev-api` 浠ｇ悊宸茶皟鏁翠负 `http://localhost:8080`锛岀敤浜庡尮閰嶅綋鍓嶆湰鍦?`ruoyi-gateway` 榛樿绔彛銆傝嫢閮ㄧ讲鐜鎴?Nacos 涓綉鍏崇鍙ｆ敼鍥?`8088`锛岄渶瑕佸悓姝ヨ皟鏁磋浠ｇ悊鍦板潃锛屾垨淇濊瘉缃戝叧瀹為檯鐩戝惉绔彛涓庡墠绔唬鐞嗕竴鑷淬€?
# 鍥㈤槦鏁村悎璇存槑鏂囨。

鏈枃妗ｇ敤浜庤褰曞钩鍙板悗缁笌鍏朵粬鍥㈤槦鏁村悎鏃堕渶瑕佸悓姝ョ殑鍐呭銆備互鍚庢瘡娆″畬鎴愬钩鍙扮浉鍏充换鍔″墠锛岄兘闇€瑕佹鏌ユ湰鏂囦欢鏄惁闇€瑕佹洿鏂般€?
## 浠€涔堟椂鍊欓渶瑕佹洿鏂版湰鏂囨。

鍙浠诲姟娑夊強涓嬮潰浠绘剰鍐呭锛屽氨闇€瑕佸悓姝ユ洿鏂版湰鏂囨。锛?

- 澶栭儴鏈嶅姟鍦板潃銆佺鍙ｃ€佽姹傚弬鏁般€佽繑鍥炲瓧娈靛彂鐢熷彉鍖栥€?
- 鏁版嵁搴撹〃銆佸瓧娈点€佸垵濮嬪寲鑴氭湰銆佸崌绾ц剼鏈€佹潈闄愭暟鎹彂鐢熷彉鍖栥€?
- 鍥㈤槦涔嬮棿浜ゆ崲鐨勬枃浠舵牸寮忓彂鐢熷彉鍖栵紝渚嬪 STEP銆丳arasolid銆丼TL銆佸浘鐗囥€丒xcel銆佹ā鍨嬫枃浠躲€佷豢鐪熺粨鏋滃寘绛夈€?
- 鍚姩鏂瑰紡銆佺幆澧冨彉閲忋€侀儴缃查『搴忋€佹湰鍦拌蒋浠惰矾寰勫彂鐢熷彉鍖栥€?
- 浠诲姟娴佺▼鐘舵€併€佸鎵硅鍒欍€佽彍鍗曟潈闄愩€佺敤鎴疯鑹插彂鐢熷彉鍖栥€?
- SolidWorks銆丄NSYS銆丳ython 浠ｇ悊妯″瀷鎴栧叾浠栫涓夋柟宸ュ叿鐨勯泦鎴愭柟寮忓彂鐢熷彉鍖栥€?
- 閿欒鎻愮ず銆侀噸璇曢€昏緫銆佹棩蹇楁枃浠躲€佽緭鍑虹洰褰曞彂鐢熷彉鍖栥€?

濡傛灉涓€娆′换鍔′笉褰卞搷鍥㈤槦鏁村悎锛屽垯涓嶉渶瑕佷慨鏀规湰鏂囨。銆?

## 褰撳墠骞冲彴妯″潡

| 妯″潡 | 鑱岃矗 | 涓昏鐩綍 | 鏁村悎璇存槑 |
| --- | --- | --- | --- |
| RuoYi Java 鍚庣 | 浠诲姟娴佺▼銆佹暟鎹繚瀛樸€佹湇鍔＄紪鎺?| `ruoyi-modules/ruoyi-designtask1` | 璐熻矗璋冪敤 Python銆丼olidWorks銆丄NSYS 绛夊閮ㄦ湇鍔°€?|
| RuoYi 鍓嶇 | 璁捐浠诲姟銆佹眰瑙ｃ€丆AD銆佷豢鐪熴€佸綊妗ｉ〉闈?| `ruoyi-ui/src/views/designtask` | 椤甸潰璇锋眰鍙傛暟闇€瑕佸拰鍚庣鎺ュ彛淇濇寔涓€鑷淬€?|
| Python 浠ｇ悊妯″瀷鏈嶅姟 | 浠ｇ悊妯″瀷浼樺寲姹傝В | `python/project2` | 榛樿绔彛 `9721`銆?|
| SolidWorks Worker | 鏍规嵁璁捐鍙橀噺鐢熸垚绠¤矾 CAD 妯″瀷 | `solidworks_worker` | 榛樿绔彛 `18080`锛岃緭鍑?SLDPRT銆丼TEP銆丳arasolid銆丼TL銆侀瑙堝浘銆?|
| ANSYS Worker | 灏?CAD 鍑犱綍瀵煎叆 ANSYS Workbench锛屽苟灏濊瘯鎵ц Mechanical 缁撴瀯姹傝В | `ansys_worker` | 榛樿绔彛 `18081`锛岄渶瑕侀厤缃?`ANSYS_WORKBENCH_CMD`銆?|
| SQL 鑴氭湰 | 琛ㄧ粨鏋勩€佸垵濮嬪寲鏁版嵁銆佸崌绾ц剼鏈?| `sql` | 娑夊強琛ㄧ粨鏋勫彉鍖栨椂锛岄渶瑕佹彁渚涘崌绾ц剼鏈€?|

## 鏈嶅姟鎺ュ彛娓呭崟

| 鏈嶅姟 | 鎺ュ彛 | 鐢ㄩ€?|
| --- | --- | --- |
| Python 浠ｇ悊妯″瀷鏈嶅姟 | `POST http://127.0.0.1:9721/api/surrogate/optimize` | 鍚姩浠ｇ悊妯″瀷浼樺寲姹傝В銆?|
| Python 鍋ュ悍妫€鏌?| `GET http://127.0.0.1:9721/health` | 妫€鏌ヤ唬鐞嗘ā鍨嬫湇鍔℃槸鍚﹀彲鐢ㄣ€?|
| SolidWorks Worker | `POST http://127.0.0.1:18080/api/pipe-model` | 鏍规嵁璁捐鍙橀噺鐢熸垚 CAD 鏂囦欢銆?|
| ANSYS Worker | `POST http://127.0.0.1:18081/api/ansys/import-geometry` | 灏?CAD 鍑犱綍瀵煎叆 Workbench锛屽苟鍦?`transient_structural` 妯″紡涓嬭皟鐢?Mechanical 姹傝В銆?|
| ANSYS Worker 鍋ュ悍妫€鏌?| `GET http://127.0.0.1:18081/api/ansys/health` | 妫€鏌?ANSYS Worker 鏄惁鍚姩銆乄orkbench 璺緞鏄惁閰嶇疆銆佸疄闄呰В鏋愬埌鐨勫惎鍔ㄦ枃浠舵槸鍚﹀瓨鍦ㄣ€?|

鍚庣榛樿閰嶇疆椤癸細

```properties
designtask.surrogate.url=http://127.0.0.1:9721/api/surrogate/optimize
designtask.solidworks.worker-url=http://127.0.0.1:18080/api/pipe-model
designtask.ansys.worker-url=http://127.0.0.1:18081/api/ansys/import-geometry
```

## 鏈湴宸ュ叿閰嶇疆瑕佹眰

### SolidWorks

- Worker 鎵€鍦ㄦ満鍣ㄩ渶瑕佸畨瑁?SolidWorks銆?
- 闇€瑕佹敮鎸?SolidWorks COM 鑷姩鍖栥€?
- 閫氳繃 `solidworks_worker/start_pipe_worker.bat` 鍚姩鏈湴鏈嶅姟銆?
- 姝ｅ父鎯呭喌涓嬪簲鐢熸垚浠ヤ笅鏂囦欢锛?
  - `pipe_native.SLDPRT`
  - `pipe_model.step`
  - `pipe_model.x_t`
  - `pipe.stl`
  - `pipe_preview.png`

### ANSYS Workbench

鍚姩 ANSYS Worker 鍓嶏紝闇€瑕佸皢 `ANSYS_WORKBENCH_CMD` 閰嶇疆涓烘湰鏈?Workbench 瀹樻柟鍚姩鑴氭湰 `runwb2.bat` 鐨勫畬鏁磋矾寰勩€?

绀轰緥锛?

```bat
set "ANSYS_WORKBENCH_CMD=D:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat"
```

蹇呴』閰嶇疆鐪熷疄鏂囦欢璺緞锛屼笉鑳介厤缃?Windows 寮€濮嬭彍鍗曚腑鐨?`.lnk` 蹇嵎鏂瑰紡銆俙runwb2.bat` 浼氬厛璁剧疆 ANSYS 杩愯鐜鍐嶅惎鍔?Workbench锛涘鏋滈厤缃负鍚岀洰褰曠殑 `RunWB2.exe`锛孉NSYS Worker 浼氳嚜鍔ㄥ垏鎹㈠埌鏃佽竟鐨?`runwb2.bat`锛岄伩鍏?Workbench 鎵瑰鐞嗗惎鍔ㄥ嵆閫€鍑恒€?

濡傛灉杩斿洖 `Ansys.Utilities.Registry.RegistryException`锛岃〃绀?Workbench 鍦ㄦ墽琛?Journal 鍓嶇殑妗嗘灦鍒濆鍖栭樁娈靛け璐ワ紝杩樻病鏈夊紑濮嬪鍏ュ嚑浣曟垨姹傝В銆傛鏃跺簲鍏堝叧闂墍鏈?ANSYS/Mechanical 杩涚▼锛岀敤 `runwb2.bat` 閲嶆柊鍚姩 Worker锛涘鏋滀粛澶辫触锛岄渶瑕侀噸鍚?Windows 鎴栦慨澶?ANSYS Workbench 鐢ㄦ埛閰嶇疆/瀹夎 registry銆?

鐒跺悗鍚姩锛?

```bat
ansys_worker\start_ansys_worker.bat
```

## 鏁版嵁鍜屾枃浠剁害瀹?

### CAD 杈撳嚭绾﹀畾

SolidWorks Worker 杩斿洖鐨勫叧閿瓧娈碉細

| 瀛楁 | 鍚箟 |
| --- | --- |
| `sldprtPath` | SolidWorks 鍘熺敓闆朵欢鏂囦欢璺緞銆侫NSYS 瀵煎叆鏃剁浜屼紭鍏堜娇鐢ㄣ€?|
| `stepPath` | STEP 鏂囦欢璺緞锛岀敤浜庡閮?CAD/CAE 杞欢瀵煎叆銆侫NSYS 瀵煎叆鏃剁涓€浼樺厛浣跨敤銆?|
| `parasolidPath` | Parasolid 鏂囦欢璺緞銆傚綋鍓嶇幆澧冧腑璇ユ牸寮忓鍏?SpaceClaim 涓嶇ǔ瀹氾紝浣滀负绗笁浼樺厛绾у厹搴曘€?|
| `stlPath` | STL 鏂囦欢璺緞锛岀敤浜庨瑙堟垨鍑犱綍璋冭瘯銆?|
| `centerlineCsvPath` | 绠￠亾涓績绾?CSV 璺緞銆侫NSYS Worker 鐢ㄥ畠缁撳悎鍐呭緞璇嗗埆鍐呭鍘嬪姏闈€?|
| `previewPngPath` | CAD 棰勮鍥捐矾寰勩€?|

CAD Worker 鐜板湪瑕佹眰绠￠亾鎴潰鍚屾椂鍖呭惈澶栧緞鍜屽唴寰勶細

```text
pipeDiameter / pipe_outer_diameter_mm      绠￠亾澶栧緞锛屽崟浣?mm
pipeInnerDiameter / pipe_inner_diameter_mm 绠￠亾鍐呭緞锛屽崟浣?mm
```

SolidWorks 鐢熸垚鐨?`pipe_native.SLDPRT`銆乣pipe_model.step` 鍜?`pipe_model.x_t` 蹇呴』鏄湡瀹炵┖蹇冪銆俉orker 浼氬厛鎵帬澶栧緞锛屽啀鎶藉３鎴栧垏闄ゅ唴瀛旓紱濡傛灉鍐呭瓟鏃犳硶鐢熸垚锛屼换鍔″簲澶辫触锛屼笉鑳芥妸瀹炲績妯″瀷浜ょ粰 ANSYS銆?

### ANSYS 鍑犱綍瀵煎叆绾﹀畾

鍚庣閫夋嫨鍑犱綍鏂囦欢鐨勯『搴忎负锛?

1. `stepPath`
2. `sldprtPath`
3. `parasolidPath`
4. `stlPath`

鍏朵腑 STEP 鏄綋鍓嶅钩鍙颁紭鍏堜娇鐢ㄧ殑涓€у嚑浣曟牸寮忥紱`SLDPRT` 鏄凡鍦?SolidWorks 涓獙璇佸彲鎵撳紑鐨勫師鐢熷厹搴曟牸寮忥紱Parasolid 鍦ㄥ綋鍓嶇幆澧冧腑鍙兘鍑虹幇 SpaceClaim 鏃犳硶缂栬緫鐨勯棶棰橈紝鍥犳涓嶅啀浣滀负绗竴浼樺厛绾с€?

褰撳墠骞冲彴姝ｅ紡鏀寔 `DEMO_SIMULATION_MODEL` 鍜?`BIDIRECTIONAL_FSI_MODEL` 涓や釜 `simulationMode`銆傚叾涓?`DEMO_SIMULATION_MODEL` 浼氭墽琛?Mechanical 绛夋晥闈欏姏缁撴瀯姹傝В锛屽湪鍏ュ彛宄板€煎帇鍔涗笅鐢熸垚搴斿姏浜戝浘锛涙棫鍊?`static_structural` 浼氬吋瀹规槧灏勫埌璇ユ紨绀烘ā鍨嬨€傞潤鍔涙ā寮忎紭鍏堝皾璇?`Static Structural`銆乣闈欐€佺粨鏋刞 妯℃澘锛屽苟浼樺厛浣跨敤 `Solver=ANSYS` 鏌ユ壘锛屼互鍏煎鑻辨枃/涓枃 Workbench銆傝剼鏈娇鐢ㄩ粯璁ゆ潗鏂欏拰鑷姩缃戞牸锛屼互鍑犱綍 X 鏂瑰悜涓ょ绔潰浣滀负鍥哄畾绾︽潫锛屽苟缁撳悎 `pipe_centerline.csv`銆佺閬撳寰勫拰绠￠亾鍐呭緞璇嗗埆绌哄績绠″唴澹侀潰鏂藉姞鍏ュ彛鍐呭帇銆傚叆鍙ｈ浇鑽蜂紭鍏堣鍙栧钩鍙颁紶鍏ョ殑鏁呴殰绠℃鍙傛暟锛歚INLET_PRESSURE_INITIAL`銆乣INLET_PRESSURE_PEAK`銆乣INLET_PRESSURE_RISE_TIME`銆乣INLET_PRESSURE_EXPRESSION`锛涙紨绀烘ā鍨嬩娇鐢?`INLET_PRESSURE_PEAK`銆傞粯璁ょ綉鏍煎昂瀵镐负 `3 mm`锛屽彲閫氳繃 `ANSYS_MESH_SIZE_MM` 瑕嗙洊銆傚鏋滃唴澹侀潰鏃犳硶璇嗗埆锛孉NSYS Worker 浼氬け璐ラ€€鍑猴紝涓嶅啀閫€鍥炵闈㈣浇鑽凤紝閬垮厤寰楀埌涓嶇鍚堝伐绋嬪疄闄呯殑缁撴灉銆?

ANSYS 2022 R1 涓儴鍒?Workbench 瀹瑰櫒瀵硅薄涓嶆敮鎸?`.Update()` 鏂规硶锛學orker 宸查噰鐢ㄥ畨鍏ㄦ洿鏂伴€昏緫锛氭敮鎸佸垯璋冪敤锛屼笉鏀寔鍒欒烦杩囷紝骞堕€氳繃 `workbench_steps.txt` 璁板綍鎵ц杩涘害銆?

Workbench 鍚?Mechanical 鍙戦€佹寮忚剼鏈墠锛屼細鍏堝彂閫佹渶灏忔彙鎵嬪懡浠わ紱鐢熸垚 `mechanical_command_ready.txt` 琛ㄧず Workbench 鍒?Mechanical 鐨勫懡浠ら€氶亾宸叉墦閫氥€傛彙鎵嬫垚鍔熷悗鍐嶉€氳繃 `execfile(...)` 鎵ц `mechanical_setup.py`锛屽苟绛夊緟 `mechanical_started.txt` 纭姝ｅ紡鑴氭湰宸茬粡寮€濮嬫墽琛屻€傝剼鏈繍琛屼腑浼氬啓鍏?`mechanical_trace.txt`锛岀敤浜庡畾浣嶅綋鍓嶅浜?`MESHING`銆乣SOLVING`銆乣SOLVED` 绛夐樁娈碉紱鏈€缁堢粨鏋滃啓鍏?`mechanical_result.json`銆?

褰撳墠榛樿浣跨敤浜や簰鏂瑰紡鎵撳紑 Mechanical锛坄ANSYS_MECHANICAL_INTERACTIVE=1`锛夛紝渚夸簬纭 Mechanical 鏄惁鐪熸鍚姩銆傝嫢闇€瑕佸悗鍙版ā寮忥紝鍙皢璇ュ彉閲忚涓?`0`锛屼絾涓嶅悓 ANSYS 鐗堟湰瀵瑰悗鍙?`SendCommand` 鐨勬敮鎸佸彲鑳戒笉涓€鑷淬€傚彲閫氳繃 `ANSYS_MECHANICAL_HANDSHAKE_RETRIES` 鍜?`ANSYS_MECHANICAL_HANDSHAKE_WAIT` 璋冩暣鍛戒护閫氶亾鎻℃墜閲嶈瘯娆℃暟鍜屽崟娆＄瓑寰呮椂闂淬€?

ANSYS Worker 浼氬皢浜戝浘瀵煎嚭涓轰换鍔＄洰褰曚笅鐨?`equivalent_stress.png`銆侸ava 鍚庣涓嶇洿鎺ユ妸鏈満纾佺洏璺緞浜ょ粰娴忚鍣ㄥ睍绀猴紝鑰屾槸閫氳繃 `GET /designtask/task/{taskId}/ansys-simulation/image` 杩斿洖鍥剧墖鏂囦欢锛涘墠绔娇鐢ㄥ甫鐧诲綍鍑瘉鐨勬帴鍙ｈ姹傚浘鐗囧苟鐢熸垚涓存椂棰勮鍦板潃銆?

浜戝浘瀵煎嚭榛樿浣跨敤楂樺垎杈ㄧ巼 `1920 x 1080`锛屽彲閫氳繃 `ANSYS_IMAGE_EXPORT_WIDTH` 鍜?`ANSYS_IMAGE_EXPORT_HEIGHT` 璋冩暣銆傚墠绔〉闈㈡彁渚涘師鍥鹃瑙堬紝閬垮厤缂╂斁鍚庡浘渚嬪拰鏈€澶?鏈€灏忓€兼爣娉ㄨ繃灏忋€?

鐢变簬 ANSYS 2022 R1 瀵逛腑鏂囨渶澶?鏈€灏忓€?callout 鐨勫浘褰㈠鍑哄瓧浣撴敮鎸佷笉绋冲畾锛孉NSYS Worker 浼氬湪 PNG 瀵煎嚭鍚庝娇鐢?Windows 涓枃瀛椾綋閲嶆柊缁樺埗鈥滄渶澶у簲鍔涚偣 / 鏈€灏忓簲鍔涚偣鈥濇爣娉紝閬垮厤骞冲彴绔睍绀轰贡鐮佹垨缂哄瓧銆?

Mechanical 姹傝В闃舵浣跨敤 `analysis.Solve(True)` 瑙﹀彂鍒嗘瀽绯荤粺姹傝В锛屽苟鍦ㄧ粨鏋?JSON 涓褰?`solutionStatus`銆乣stressStatus`銆乣deformationStatus`銆乣warnings`銆乣maxEquivalentStressValue` 鍜?`maxTotalDeformationValue`銆傚鏋滃叆鍙ｅ帇鍔涢潪 0 浣嗙瓑鏁堝簲鍔涘拰鎬诲彉褰粛鍚屾椂涓?0锛學orker 浼氬皢璇ユ缁撴灉鏍囪涓哄け璐ワ紝闃叉骞冲彴鎶婃棤鏁堢粨鏋滄樉绀轰负鎴愬姛銆?
Worker 杩樹細鍩轰簬鍐呭帇钖勫绠″叕寮忚绠楀悕涔夌幆鍚戝簲鍔涖€佸悕涔夎酱鍚戝簲鍔涘拰鍚嶄箟 Von-Mises 搴斿姏锛屽苟缁撳悎鏉愭枡灞堟湇寮哄害銆佹媺浼告瀬闄愬己搴﹀拰涓績绾块暱搴﹀啓鍏?`engineeringStatus`銆乣engineeringWarnings`銆乣engineeringEstimates`銆傝嫢鏈€澶у簲鍔涜秴杩囧悕涔夊簲鍔?10 鍊嶃€佽秴杩囨潗鏂欐媺浼告瀬闄愬己搴︼紝鎴栨渶澶у彉褰㈣秴杩囩涓績绾块暱搴?20%锛屾湰娆＄粨鏋滀細鏍囪涓哄け璐ワ紝闇€瑕佸鏍歌浇鑽烽潰銆佽竟鐣屾潯浠躲€佺綉鏍兼垨鏀圭敤闈炵嚎鎬фā鍨嬨€?

璇锋眰绀轰緥锛?

```json
{
  "taskId": 27,
  "geometry": {
    "geometryPath": "C:/path/to/pipe_model.x_t",
    "geometryType": "PARASOLID"
  }
}
```

鎴愬姛杩斿洖鏃堕渶瑕佸叧娉ㄧ殑瀛楁锛?

| 瀛楁 | 鍚箟 |
| --- | --- |
| `status` | `SUCCESS` 琛ㄧず瀵煎叆瀹屾垚銆?|
| `projectPath` | 鐢熸垚鐨?Workbench 椤圭洰鏂囦欢璺緞銆?|
| `resultFilePath` | 涓昏缁撴灉鏂囦欢璺緞銆?|
| `workDir` | Worker 杈撳嚭鐩綍銆?|
| `metrics` | 瀵煎叆鐘舵€佸拰鍩虹鏂囦欢淇℃伅銆?|

## 鏁版嵁搴撴暣鍚堟鏌ラ」

涓庡叾浠栧洟闃熸暣鍚堟椂锛岄渶瑕佺‘璁ゆ槸鍚﹂渶瑕佹柊澧炴垨璋冩暣浠ヤ笅鍐呭锛?

- 澶栭儴绯荤粺浠诲姟 ID銆佽拷韪?ID銆?
- 鏉ユ簮绯荤粺鍚嶇О鍜岀増鏈€?
- 杈撳叆鏂囦欢璺緞銆佹枃浠剁被鍨嬨€佹牎楠屽€笺€佷笂浼犵敤鎴枫€?
- 杈撳嚭鏂囦欢璺緞銆佺粨鏋滅姸鎬併€侀敊璇俊鎭€?
- 浠跨湡鎸囨爣銆佸崟浣嶃€佺粨鏋滃浘鐗囥€?
- 瀹℃壒鐘舵€佸拰鏈€缁堝綊妗ｅ揩鐓с€?

褰撳墠 CAD/ANSYS 鐩稿叧鍗囩骇鑴氭湰锛?

```text
sql/t2_cad_ansys_geometry_columns.sql
```

## 鏉冮檺鍜岃彍鍗曟鏌ラ」

鏂板鎴栫Щ闄ゆā鍧楁椂锛岄渶瑕佹鏌ワ細

- 鍓嶇璺敱鍜屼晶杈规爮鑿滃崟銆?
- 鍚庣鏉冮檺鏍囪瘑鍜屾帶鍒跺櫒鎺ュ彛銆?
- 鑿滃崟銆佽鑹层€佹寜閽潈闄愮殑 SQL 鍒濆鍖栨暟鎹€?
- 鑰佽彍鍗曟槸闅愯棌銆佸垹闄わ紝杩樻槸涓轰簡鍏煎缁х画淇濈暀銆?

褰撳墠璁捐娴佺▼涓凡缁忕Щ闄や簡鈥滄爣鍑嗚祫婧愮鐞嗏€濊彍鍗曘€傚鏋滃叾浠栧洟闃熶粛渚濊禆璇ュ叆鍙ｏ紝闇€瑕佸厛瀹氫箟鏇夸唬鍏ュ彛锛屽啀鑰冭檻鎭㈠銆?

## 瀵瑰绉讳氦妫€鏌ユ竻鍗?

骞冲彴浜ょ粰鍏朵粬鍥㈤槦鍓嶏紝闇€瑕佸噯澶囷細

- 鏈嶅姟鍚姩椤哄簭銆?
- 绔彛鍜岄槻鐏瑕佹眰銆?
- 鐜鍙橀噺鍜屾湰鍦拌蒋浠跺畨瑁呰矾寰勩€?
- 鏁版嵁搴撳垵濮嬪寲鑴氭湰鍜屽崌绾ц剼鏈€?
- API 璇锋眰涓庡搷搴旂ず渚嬨€?
- 鏂囦欢浜ゆ崲鐩綍绾﹀畾銆?
- 浠诲姟鐘舵€佹祦杞鏄庛€?
- 宸茬煡闄愬埗鍜屽父瑙佸け璐ユ彁绀恒€?
- 娴嬭瘯鏁版嵁鍜屼竴鏉″畬鏁存紨绀轰换鍔°€?

## 姣忔浠诲姟缁撴潫鍓嶆鏌?

浠ュ悗姣忔浠诲姟鏀跺熬鍓嶏紝闇€瑕佹鏌ワ細

1. 鏄惁淇敼浜嗘帴鍙ｃ€佸瓧娈靛悕鎴栬繑鍥炵粨鏋勶紵
2. 鏄惁鏂板鎴栦慨鏀逛簡鏁版嵁搴撹〃銆佸瓧娈垫垨鑴氭湰锛?
3. 鏄惁淇敼浜嗗閮ㄦ枃浠舵牸寮忔垨鐢熸垚璺緞锛?
4. 鏄惁淇敼浜嗗惎鍔ㄦ楠ゃ€佺鍙ｆ垨鐜鍙橀噺锛?
5. 鏄惁褰卞搷浜嗚鑹层€佽彍鍗曟垨鏉冮檺锛?
6. 鏄惁鏂板浜嗗鍏朵粬鍥㈤槦绯荤粺鐨勪緷璧栵紵
7. 鏄惁淇敼浜嗘帓鏌ラ棶棰樼殑鏂规硶鎴栧凡鐭ラ檺鍒讹紵

濡傛灉浠ヤ笂浠绘剰涓€椤逛负鈥滄槸鈥濓紝灏遍渶瑕佹洿鏂版湰鏂囨。銆?

## Project22 妗嗘瑁傜汗鎵╁睍涓庡鍛介娴?

鏈鏂板妗嗘瑁傜汗鎵╁睍涓庡鍛介娴嬭兘鍔涳紝闇€涓庣幇鏈夋恫鍘嬪集绠℃姉鍐插嚮浼樺寲浠诲姟淇濇寔闅旂銆傝缁嗘枃浠舵竻鍗曡鏍圭洰褰?`project22readme.md`銆?

鏂板浠诲姟绫诲瀷锛?

```text
FRAME_BEAM_CRACK_LIFE_PREDICTION
```

鏂板 Python 浠ｇ悊妯″瀷鏈嶅姟锛?

```text
GET  http://127.0.0.1:9822/health
POST http://127.0.0.1:9822/api/frame-beam-crack/growth-predict
```

鍚庣閰嶇疆椤癸細

```properties
designtask.frame-beam.surrogate-base-url=http://127.0.0.1:9822
```

鏂板 Java 鍚庣鎺ュ彛锛?

```text
GET  /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-load-spectrum
GET  /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-maintenance-advice/confirm
```

鏂板鏁版嵁搴撹剼鏈細

```text
sql/t2_frame_beam_crack_life_schema.sql
sql/t2_frame_beam_crack_life_menu.sql
```

鏂板鍓嶇鐩綍锛?

```text
ruoyi-ui/src/views/designtask/frameBeam/
```

杞借嵎璋辩害瀹氾細

- 鏀寔 CSV銆丒xcel銆丣SON 鏂囦欢璁板綍銆?
- 鏍稿績瀛楁鍖呮嫭 `maxStress`銆乣minStress`銆乣cycles`銆乣currentFlightHours`銆?
- 搴斿姏鍗曚綅榛樿 MPa锛岃绾归暱搴﹂粯璁?mm銆?
# 杩戞湡鏇存柊锛歋olidWorks 绌哄績绠′笌 ANSYS 鍐呭璇嗗埆

鏈淇褰卞搷 CAD/ANSYS 鏂囦欢浜ゆ崲涓庢帓閿欐柟寮忥細

- SolidWorks Worker 鐢熸垚绠￠亾鏃讹紝`pipe_native.SLDPRT`銆乣pipe_model.step`銆乣pipe_model.x_t` 蹇呴』鏄湡姝ｈ疮閫氱殑绌哄績绠★紝涓嶈兘鍙敓鎴愬瀹炰綋鎴栦竴绔皝闂殑妯″瀷銆?
- 鍐呭瓟鍒囬櫎璺緞浼氫娇鐢?`wallCutExtension / wall_cut_extension_mm` 鍚戜袱绔欢闀匡紝瀹為檯寤堕暱閲忚嚦灏戜负 `max(閰嶇疆鍊? 澶栧緞 * 3, 20mm)`锛岀敤浜庨伩鍏嶇閮ㄦ畫鐣欏皝鐩栥€?
- 濡傛灉 SolidWorks 鏃犳硶瀹屾垚鎶藉３鎴栧唴瀛斿垏闄わ紝Worker 蹇呴』杩斿洖澶辫触锛屼笉鍏佽鎶婂疄蹇冪妯″瀷缁х画浼犵粰 ANSYS銆?
- ANSYS Worker 璇嗗埆鍐呭鍘嬪姏闈㈡椂浼氬悓鏃跺皾璇曠背鍜屾绫充袱绉嶅昂搴︼紝閬垮厤 Workbench/Mechanical 杩斿洖鍑犱綍鍧愭爣鍗曚綅涓?`pipe_centerline.csv` 涓嶄竴鑷村鑷磋鍒ゃ€?
- 鎺掓煡 ANSYS 鍐呭璇嗗埆鏃讹紝浼樺厛鏌ョ湅浠诲姟鐩綍涓嬬殑 `mechanical_trace.txt`锛屽叾涓細杈撳嚭 `INNER_WALL_UNIT`銆乣INNER_WALL_TARGET_RADIUS`銆乣INNER_WALL_TOLERANCE`銆乣INNER_WALL_FACES` 鍜?`FACE_INFO`銆?

琛ュ厖锛氬鏋?SolidWorks 瀵规暣鏉″集鏇蹭腑蹇冪嚎鐨勫渾褰㈡壂鎺犲垏闄ゅ湪绔儴鐣欎笅灏佺洊锛學orker 浼氶澶栫敓鎴?`InnerBoreStartOpen3D` 鍜?`InnerBoreEndOpen3D` 涓ゆ潯鐭洿绾垮紑鍙ｅ垏闄よ矾寰勶紝鐢ㄥ悓涓€鍐呭緞鍦ㄤ袱绔悇鑷墦閫氾紝纭繚瀵煎嚭鐨?STEP/SLDPRT 鏄弻绔疮閫氱┖蹇冪銆?

琛ュ厖锛欰NSYS 鍐呭鍘嬪姏闈㈣瘑鍒椂蹇呴』鎺掗櫎涓ょ绔潰銆傜閮ㄧ幆褰㈤潰铏界劧灞炰簬绌哄績绠″鍘氬疄浣擄紝浣嗕笉鏄唴澹佸渾鏌遍潰锛屼笉鑳芥柦鍔犲叆鍙ｅ唴鍘嬶紱`mechanical_trace.txt` 涓?`FACE_INFO` 浼氭爣璁?`endFace=True/False`锛屽彧鏈夐潪绔潰涓斾腑蹇冪嚎璺濈鍖归厤鍐呭崐寰勭殑闈㈡墠浼氫綔涓?`INNER_WALL_FACES`銆?

琛ュ厖锛欰NSYS Worker 鐨?`mechanical_result.json.status` 鍙〃绀?Mechanical 鏄惁瀹屾垚鏈夋晥姹傝В銆傚簲鍔涜秴杩囨潗鏂欏己搴︺€丗E 宄板€艰繙楂樹簬钖勫绠″悕涔夊簲鍔涖€佸彉褰㈣秴杩囩闀?5% 绛夊睘浜庡伐绋嬪鏍搁璀︼紝搴旈€氳繃 `engineeringStatus=REVIEW_REQUIRED`銆乣engineeringWarnings` 鍜?`engineeringEstimates` 杩斿洖缁欏钩鍙板睍绀猴紝涓嶅簲鍐嶈Е鍙?HTTP 500 鎴栨樉绀轰负 `ANSYS Worker unavailable`銆?

琛ュ厖锛氬綋鍓?Mechanical 鑷姩姹傝В榛樿閲囩敤涓ょ鍥哄畾鏀拺锛屽嵆鍑犱綍 X 鏂瑰悜鏈€灏忕闈㈠拰鏈€澶х闈㈠悓鏃朵綔涓?`Fixed Support`锛沗mechanical_trace.txt` 浼氬啓鍏?`FIXED_SUPPORT_MODE=both_ends`銆乣FIXED_SUPPORT_MIN_X`銆乣FIXED_SUPPORT_MAX_X` 鍜?`FIXED_SUPPORT_FACES`銆侫NSYS Worker 榛樿璁剧疆 `ANSYS_KEEP_MECHANICAL_OPEN=1`锛屾眰瑙ｅ畬鎴愬悗淇濈暀 Mechanical 绐楀彛锛屼究浜庝汉宸ユ鏌ユā鍨嬫爲銆佺綉鏍笺€佽浇鑽峰拰缁撴灉锛涘闇€鎵瑰鐞嗚嚜鍔ㄥ叧闂紝鍙缃负 `0`銆?

# 杩戞湡鏇存柊锛欰NSYS 鍙屾ā鍨嬩豢鐪熼€夋嫨

骞冲彴浠跨湡纭椤电幇鍦ㄦ敮鎸佸湪寮€濮?ANSYS 浠跨湡鍓嶉€夋嫨浠跨湡妯″瀷銆傚墠绔€丣ava 鍚庣鍜?ANSYS Worker 缁熶竴浣跨敤 `simulationMode` 鍖哄垎缁撴灉锛岄伩鍏嶄袱涓ā鍨嬩簰鐩歌鐩栥€?

| 灞曠ず鍚嶇О | simulationMode | 鐢ㄩ€?|
| --- | --- | --- |
| 婕旂ず浠跨湡妯″瀷 | `DEMO_SIMULATION_MODEL` | 淇濈暀褰撳墠 Mechanical 闈欏姏缁撴瀯娴佺▼锛岀敤浜庡钩鍙板睍绀恒€佸揩閫熸牎鏍稿拰搴斿姏浜戝浘杈撳嚭銆?|
| 鍙屽悜娴佸浐鑰﹀悎浠跨湡妯″瀷 | `BIDIRECTIONAL_FSI_MODEL` | 鎸夊弬鑰冩ā鍨嬪弬鏁板垱寤?Fluent + Transient Structural + System Coupling 鍙屽悜鑰﹀悎宸ョ▼鏂囦欢銆?|

鏂板/璋冩暣鐨勬帴鍙ｇ害瀹氾細

- `POST /designtask/task/{taskId}/ansys-simulation` 璇锋眰浣撳彲浼犲叆 `simulationMode`銆?
- `GET /designtask/task/{taskId}/ansys-simulation` 鏀寔 `simulationMode` 鏌ヨ鍙傛暟銆?
- `GET /designtask/task/{taskId}/ansys-simulation/image` 鏀寔 `simulationMode` 鏌ヨ鍙傛暟锛屽苟閫氳繃鍚庣璇诲彇鏈満鍥剧墖鏂囦欢杩斿洖缁欐祻瑙堝櫒銆?

鏁版嵁琛?`t2_design_ansys_simulation_task` 鏂板 `simulation_mode` 瀛楁锛屽苟浠?`(task_id, simulation_mode)` 浣滀负涓婚敭锛涘崌绾ц剼鏈?`sql/t2_ansys_simulation_placeholder.sql` 宸插悓姝ャ€傚巻鍙叉湭鍖哄垎妯″紡鐨勬暟鎹細鎸?`DEMO_SIMULATION_MODEL` 鍏煎銆?

ANSYS Worker 杈撳嚭鐩綍鎸夋ā鍨嬫媶鍒嗭細

```text
ansys_worker/output/task_{taskId}/demo_simulation_model/
ansys_worker/output/task_{taskId}/bidirectional_fsi_model/
```

褰撳墠 `BIDIRECTIONAL_FSI_MODEL` 鍒嗘敮浼氱敓鎴愬弻鍚戞祦鍥鸿€﹀悎鍙傝€冨伐绋嬨€侀厤缃?JSON 鍜?Fluent 璁剧疆璇存槑鏂囦欢锛岀敤浜庝笌鍙傝€?`1.wbpz` 鐨勫弬鏁颁繚鎸佷竴鑷淬€傝嫢瑕佸仛鍒板叏鑷姩鐪熷疄鍙屽悜鑰﹀悎姹傝В锛岃繕闇€瑕佸悗缁ˉ榻愭祦浣撳煙鍑犱綍銆佸叆鍙?鍑哄彛/澹侀潰鍛藉悕杈圭晫銆丗luent 缃戞牸涓?System Coupling 鏁版嵁浼犻€掔殑鍙墽琛岃嚜鍔ㄥ寲鑴氭湰銆?

# 杩戞湡鏇存柊锛氳棰樹簩鏁版嵁搴撴暣鍚堣縼绉昏剼鏈?

涓轰究浜庡悎骞跺埌 `project2-dev` 骞朵氦浠樺叾浠栧洟闃熼儴缃诧紝宸叉暣鐞嗚棰樹簩涓氬姟搴撳畬鏁磋縼绉昏剼鏈細

```text
sql/t2_project2_full_migration.sql
```

璇ヨ剼鏈潰鍚戝凡鏈?RuoYi-Cloud 鍩虹搴撴墽琛岋紝鍖呭惈璇鹃浜岃〃缁撴瀯銆佸吋瀹瑰崌绾у瓧娈点€佸垵濮嬪寲鏁版嵁銆佷紭鍖栦换鍔￠缃暟鎹€佹晠闅滅娈靛弬鏁般€佹瑁傜汗瀵垮懡鑿滃崟涓庘€滄爣鍑嗚祫婧愮鐞嗏€濊彍鍗曠Щ闄ゃ€傝剼鏈腑宸插寘鍚巻鍙茶〃鍚嶅吋瀹归噸鍛藉悕閫昏緫锛岀洰鏍囧簱濡傚瓨鍦ㄦ棫琛?`design_*` 鎴?`p2_*`锛屼細鍏堣縼绉讳负 `t2_*` 鍛藉悕銆?

娉ㄦ剰浜嬮」锛?

- 鎵ц鍓嶈鍏堝浠界洰鏍囧簱銆?
- 璇ヨ剼鏈笉鍖呭惈 `flowable鐩稿叧琛?sql`锛屽洜涓哄叾涓寘鍚?Flowable 寮曟搸琛ㄩ噸寤哄拰 `DROP TABLE`锛屽彧搴斿湪骞插噣 Flowable 搴撴垨鏄庣‘闇€瑕侀噸寤烘祦绋嬪紩鎿庤〃鏃跺崟鐙墽琛屻€?
- `flowable缃戝叧璺敱.sql`銆乣nacos閰嶇疆.sql` 灞炰簬缃戝叧/Nacos 閰嶇疆搴撹剼鏈紝涓嶅缓璁贩鍏ヨ棰樹簩涓氬姟搴撹縼绉昏剼鏈紝搴旀寜閮ㄧ讲鐜鍗曠嫭纭鍚庢墽琛屻€?
