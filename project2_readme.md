## Latest Database Migration Note

For Project2 master integration, execute database scripts in this order:

1. Back up the target `ry-cloud` database.
2. Execute `sql/t2_project2_master_migration.sql` for schema, menus, roles, permissions, and base initialization data.
3. If the current local Project2 historical task records should be preserved, execute `sql/t2_project2_history_data.sql` after the master migration script.

Do not use `sql/t2_project2_full_migration.sql` as the formal master migration entry. It is an earlier integration version. Use `sql/t2_project2_master_migration.sql` as the official base migration script.
# 鐠囬箖顣芥禍灞芥値楠炶泛鍩?master 閺佹潙鎮庨幍瀣斀

閺堫剚鏋冨锝囨暏娴滃孩濡?`project2-dev` 閸掑棙鏁稉顓犳畱鐠囬箖顣芥禍宀冨厴閸旀稑鎮庨獮璺哄煂 `master`閵嗗倹鏆ｉ崥鍫滄眽閸涙ê绨查幐澶嬫拱閺傚洦鐗崇€佃鏋冩禒韬测偓浣稿彆閸忚鲸膩閸фぜ鈧焦鏆熼幑顔肩氨閼存碍婀伴妴浣规箛閸旓繝鍘ょ純顔衡偓浣割樆闁劏钂嬫禒鎯扮熅瀵板嫬鎷版灞炬暪濞翠胶鈻奸妴?

## 1. 閹恒劏宕橀崥鍫濊嫙閺傜懓绱?

娴兼ê鍘涙担璺ㄦ暏 Git 閸氬牆鑻熼敍灞肩箽閻ｆ瑦鏋冩禒璺侯杻閸掔姵鏁奸崪灞藉暱缁愪椒绗傛稉瀣瀮閿?

```powershell
git fetch origin
git checkout master
git pull origin master
git merge origin/project2-dev
```

婵″倸鍤悳鏉垮暱缁愪緤绱濋柅鎰嚋鐟欙絽鍠呴崥搴㈠⒔鐞涘矉绱?

```powershell
git add <瀹歌尪袙閸愬啿鍟跨粣浣烘畱閺傚洣娆?
git commit
git push origin master
```

婵″倹鐏夐崶銏ゆЕ鐟曚焦鐪伴幍瀣紣閺佹潙鎮庨敍灞惧瘻娑撳娼扮粩鐘哄Ν闁劙銆嶆径宥呭煑閸滃奔鎱ㄩ弨骞库偓?

## 2. 韫囧懘銆忛崥鍫濆弳閻ㄥ嫯顕虫０妯圭癌閺傚洣娆?

### 2.1 Java 娑撴艾濮熷Ο鈥虫健

閸氬牆鍙嗛弫缈犻嚋濡€虫健閿?

```text
ruoyi-modules/ruoyi-designtask1/
```

闁插秶鍋ｉ弬鍥︽閿?

```text
ruoyi-modules/ruoyi-designtask1/pom.xml
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/RuoYiDesigntask1Application.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/DesignOptimizationController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/DesignTaskController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/FrameBeamCrackController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/DesignOptimizationService.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamCrackService.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamMaintenanceAdviceService.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamPredictionClient.java
```

鏉╂瑤绨洪弬鍥︽閸栧懎鎯堥敍姘崲閸斺剝绁︽潪顑锯偓浣烘窗閺?缁撅附娼妴浣烘窗閺嶅洦娼堥柌宥呯秺閸欙絻鈧椒鍞悶鍡樐侀崹瀣湴鐟欙絻鈧竼AD 瀵ょ儤膩閵嗕竸NSYS 娴犺法婀￠妴浣锋眽瀹搞儵鐛欑拠浣虹波鐠佹亽鈧礁缍婂锝冣偓浣诡攱濮婁浇顥囩痪鐟邦嚧閸涗粙顣╁ù瀣搼闁槒绶妴?

### 2.2 閸撳秶顏い鐢告桨娑?API

閸氬牆鍙嗛敍?

```text
ruoyi-ui/src/api/designtask/
ruoyi-ui/src/views/designtask/
ruoyi-ui/src/views/project_2/
```

闁插秶鍋ｉ弬鍥︽閿?

```text
ruoyi-ui/src/api/designtask/optimization.js
ruoyi-ui/src/api/designtask/task.js
ruoyi-ui/src/views/designtask/platform-theme.scss
ruoyi-ui/src/views/designtask/dashboard/index.vue
ruoyi-ui/src/views/designtask/task/create.vue
ruoyi-ui/src/views/designtask/mechanism/index.vue
ruoyi-ui/src/views/designtask/objective/index.vue
ruoyi-ui/src/views/designtask/solve/index.vue
ruoyi-ui/src/views/designtask/simulation/index.vue
ruoyi-ui/src/views/designtask/simulation/CadStlViewer.vue
ruoyi-ui/src/views/designtask/archive/index.vue
ruoyi-ui/src/views/designtask/frameBeam/life-prediction.vue
ruoyi-ui/src/views/designtask/frameBeam/decision-advice.vue
```

閸撳秶顏潻鎴炴埂閸忔娊鏁崣妯哄閿?

- 閸楀繐鎮撻張鍝勫煑閻㈢喐鍨氭い鍨煀婢х偐鈧粎顓稿▓鐢电椽閸欏皝鈧繈鈧瀚ㄩ敍灞藉絺鐠ц渹鎹㈤崝鈩冩缂佹垵鐣鹃弫鍛存缁犫剝顔岄崣鍌涙殶韫囶偆鍙庨妴?
- 閻╊喗鐖ｇ痪锔芥将闁瀚ㄦい闈涘涧鐠愮喕鐭楅崥鍕瑩娑撴岸鈧瀚ㄩ惄顔界垼/缁撅附娼敍灞肩瑝閸愬秷顔€閸氬嫪绗撴稉姘綖閸愭瑧娲伴弽鍥ㄦ綀闁插秲鈧?
- 濡€崇€风憴锝堚偓锔界湴鐟欙綁銆夐悽杈鐠愶絼姹夌紒鐔剁瑜版帒褰涢崗銊╁劥閻╊喗鐖ｉ弶鍐櫢閿涘本娼堥柌宥堝瘱閸ョ繝璐?`0-10`閵?
- 閻╊喗鐖ｆ导妯哄缁鐎风紒鐔剁鐏炴洜銇氭稉?`min` / `max`閵?
- 閻╊喗鐖ｇ痪锔芥将閺嶏繝鐛欐稉宥呭晙鏉╂稑鍙嗘い鐢告桨閸氬酣绮拋銈夆偓姘崇箖閿涘苯绻€妞よ崵鍋ｉ崙缁樼墡妤犲苯鎮楅崝銊︹偓浣规▔缁€铏圭波閺嬫嚎鈧?
- 娴犺法婀℃宀冪槈妞ゅ灚瀵氶弽鍥ь嚠濮ｆ柧濞囬悽銊ゅ敩閻炲棙膩閸ㄥ鈧竼AD閵嗕竸NSYS 閻ㄥ嫮婀＄€圭偞鏆熼幑顕嗙礉娑撳秴鍟€娴ｈ法鏁ら崑鍥ㄦ殶閹诡喓鈧?
- 妤犲矁鐦夌紒鎾诡啈閺€閫涜礋瀹搞儳鈻肩敮鍫熷閸斻劑鈧瀚ㄩ垾婊堚偓姘崇箖/娑撳秹鈧俺绻冮垾婵撶礉娑撳秴鍟€姒涙顓婚弰鍓с仛闁俺绻冮妴?
- 鐠囬箖顣芥禍灞煎瘜鐟曚線銆夐棃銏㈢埠娑撯偓娑撹桨绗屾＃鏍€夋禒顏囥€冮惄妯圭閼峰娈戝銉ょ瑹鏉烆垯娆㈡搴㈢壐閿涘苯鑻熺悰銉ュ帠閹稿鎸崇亸蹇撴禈閺嶅洢鈧?

婵?`master` 瀹稿弶婀?`ruoyi-ui/src/router/index.js`閿涘奔绗夌憰浣烘纯閹恒儴顩惄鏍电礉鎼存梹澧滃銉ユ値楠炴儼顕虫０妯圭癌鐠侯垳鏁遍敍宀勪缉閸忓秴濂栭崫宥呭従娴犳牞顕虫０妯糕偓?

### 2.3 Python 閺堝秴濮?

閸氬牆鍙嗛敍?

```text
python/project2/
python/project22/
```

鐠囧瓨妲戦敍?

- `python/project2/`閿涙碍鎭崢瀣泦缁犫€插敩閻炲棙膩閸ㄥ绱崠鏍ㄦ箛閸斺槄绱濇妯款吇缁旑垰褰?`9721`閵?
- `python/project22/`閿涙碍顢嬪浣筋棁缁剧懓顕撮崨浠嬵暕濞村婀囬崝鈽呯礉姒涙顓荤粩顖氬經 `9822`閵?

濮濓絽绱￠柈銊ц瀵ら缚顔呮稉杞拌⒈娑擃亝婀囬崝鈥冲瀻閸掝偄缂撶粩瀣珓閹风喓骞嗘晶鍐︹偓鍌欑瑝鐟曚焦濡搁張顒佹簚娑撳瓨妞傜紓鎾崇摠閵嗕浇绻嶇悰灞炬）韫囨鍨ㄩ搹姘珯閻滎垰顣ㄩ惄顔肩秿閹绘劒姘﹂崚?master閵?

### 2.4 SolidWorks 娑?ANSYS Worker

閸氬牆鍙嗛敍?

```text
solidworks_pipe/
solidworks_worker/
ansys_worker/
```

閸欘亝褰佹禍銈嗙爱閻降鈧浇鍓奸張顒€鎷扮拠瀛樻閿涘奔绗夐幓鎰唉鏉╂劘顢戞潏鎾冲毉閿?

```text
solidworks_pipe/README.md
solidworks_pipe/create_pipe_native.vbs
solidworks_pipe/generate_pipe.py
solidworks_pipe/open_in_solidworks.vbs
solidworks_worker/README.md
solidworks_worker/pipe_worker.py
solidworks_worker/start_pipe_worker.bat
ansys_worker/README.md
ansys_worker/ansys_import_worker.py
ansys_worker/start_ansys_worker.bat
```

娑撳秷顩﹂幓鎰唉閿?

```text
ansys_worker/output/
solidworks_worker/output/
solidworks_pipe/params.json
tmp_wbpz_compare_1/
```

### 2.5 SQL 閸滃本鏆ｉ崥鍫熸瀮濡?

韫囧懘銆忛崥鍫濆弳閿?

```text
sql/t2_project2_full_migration.sql
TEAM_INTEGRATION_README.md
project2_readme.md
project22readme.md
PROJECT2_PENDING_COMMIT_FILES.md
```

閸忔湹绮?`sql/t2_*.sql` 閸欘垯浜掓担婊€璐熼幏鍡楀瀻閺夈儲绨幋鏍х湰闁劏藟娑撲礁寮懓鍐х箽閻ｆ瑥婀禒鎾崇氨娑擃叏绱濇担鍡橆劀瀵繗绺肩粔缁樻殶閹诡喖绨遍崣顏呭⒔鐞涘矉绱?

```text
sql/t2_project2_full_migration.sql
```

娑撳秷顩﹂幎?`flowable閻╃鍙х悰?sql`閵嗕梗flowable缂冩垵鍙х捄顖滄暠.sql`閵嗕梗nacos闁板秶鐤?sql` 瑜版挷缍旂拠楣冾暯娴滃奔绗熼崝鈥崇氨鏉╀胶些閼存碍婀伴惄瀛樺复閹笛嗩攽閵?

## 3. master 閸忣剙鍙″Ο鈥虫健闂団偓鐟曚礁鎮庨獮鍓佹畱閸愬懎顔?

### 3.1 閺?POM

閺傚洣娆㈤敍?

```text
pom.xml
```

闂団偓鐟曚焦鐗崇€?`project2-dev` 娑擃厽鏌婃晶鐐村灗鐠嬪啯鏆ｆ潻鍥╂畱閸忣剙鍙℃笟婵婄閻楀牊婀伴敍灞肩伐婵″偊绱?

```text
spring-boot.version
spring-cloud.version
spring-cloud-alibaba.version
spring-boot-admin.version
mybatis-spring-boot.version
springdoc.version
flowable.version
hutool.version
thumbnailator.version
tika.version
aviator.version
easyexcel.version
mybatis-plus-boot.version
jsqlparser.version
spring-integration.version
```

婵″倹鐏?`master` 瀹歌尙绮￠崡鍥╅獓閸掗绗夐崥宀€澧楅張顒婄礉娑撳秷顩﹂張鐑橆潾鐟曞棛娲婇敍宀勬付娴犮儱鍙忔禒鎾崇氨閼崇晫绱拠鎴濇嫲閸氼垰濮╂稉鍝勫櫙閵?

### 3.2 ruoyi-modules 閼辨艾鎮?POM

閺傚洣娆㈤敍?

```text
ruoyi-modules/pom.xml
```

婢х偛濮為敍?

```xml
<module>ruoyi-designtask1</module>
```

### 3.3 缂冩垵鍙ф稉搴ゎ吇鐠囦焦膩閸?

濞戝寮烽敍?

```text
ruoyi-gateway/pom.xml
ruoyi-gateway/src/main/java/com/ruoyi/gateway/RuoYiGatewayApplication.java
ruoyi-gateway/src/main/resources/bootstrap.yml
ruoyi-auth/pom.xml
ruoyi-auth/src/main/java/com/ruoyi/auth/RuoYiAuthApplication.java
ruoyi-auth/src/main/resources/bootstrap.yml
```

閺佹潙鎮庨柌宥囧仯閿?

- `DataSourceAutoConfiguration` 閻?import 閸栧懓鐭惧鍕洣娑?`master` 娴ｈ法鏁ら惃?Spring Boot 閻楀牊婀伴崠褰掑帳閵?
- 婵″倹鐏?`project2-dev` 娑擃厼顤冮崝鐘辩啊 `scanBasePackages`閿涘矂娓剁涵顔款吇 Feign閵嗕够ystem api閵嗕公allback 閼宠姤顒滅敮鍛婂閹诲繈鈧?
- `bootstrap.yml` 娑擃厺绗夌憰浣风箽閻ｆ瑦婀伴張?`127.0.0.1` 閸ュ搫鐣惧▔銊ュ斀 IP閿涙稒婀囬崝鈥虫珤闁劎璁查弮璺虹安閺€閫涜礋閺堝秴濮熼崳銊ュ敶缂?IP閿涘本鍨ㄩ崚鐘绘珟閸ュ搫鐣?IP 鐠?Nacos 閼奉亜濮╃拠鍡楀焼閵?

### 3.4 閸撳秶顏崗顒€鍙￠崗銉ュ經

濞戝寮烽敍?

```text
ruoyi-ui/package.json
ruoyi-ui/src/main.js
ruoyi-ui/src/router/index.js
```

閺佹潙鎮庨柌宥囧仯閿?

- 閸氬牆鍙嗙拠楣冾暯娴滃本鏌婃晶鐐扮贩鐠ф牕鎮楅幍褑顢?`npm install`閵?
- `router/index.js` 閸氬牆鑻熺拠楣冾暯娴滃矁鐭鹃悽鎲嬬礉娴ｅ棔绗夐懗鍊燁洬閻╂牕鍙炬禒鏍嚦妫版鐭鹃悽渚库偓?
- 閼?`main.js` 濞夈劌鍞芥禍鍡樼ウ缁嬪顔曠拋鈥虫珤閵嗕礁鍙忕仦鈧弽宄扮础閹存牜绮嶆禒璁圭礉闂団偓鐟曚椒绗?master 瑜版挸澧犻崘鍛啇閸氬牆鑻熼妴?

### 3.5 Flowable 閸撳秶顏紒鍕

婵″倹鐏?`master` 濞屸剝婀佸ù浣衡柤鐠佹崘顓搁崳銊ф祲閸忓啿澧犵粩顖涙瀮娴犺绱濋棁鈧憰浣告値閸忋儻绱?

```text
ruoyi-ui/src/api/workflow/
ruoyi-ui/src/components/ProcessDesigner/
ruoyi-ui/src/components/ProcessViewer/
ruoyi-ui/src/modules/
ruoyi-ui/src/package/
ruoyi-ui/src/utils/min-dash.js
ruoyi-ui/src/views/workflow/
```

婵″倹鐏?`master` 瀹稿弶婀侀懛顏勭箒閻?Flowable 閸撳秶顏€圭偟骞囬敍宀勬付鐟曚線鈧劙銆嶇€佃鐦敍灞肩瑝鐟曚胶娲块幒銉洬閻╂牓鈧?

## 4. 閺傛澘顤冮崪宀勬付鐟曚椒绻氶悾娆戞畱 Java API

娴犮儰绗呴幒銉ュ經閻?`ruoyi-designtask1` 閹绘劒绶甸敍灞藉缁旑垶鈧俺绻冪純鎴濆彠鐠佸潡妫?`/designtask/**`閵?

### 4.1 娴犺濮熸稉搴㈢ウ缁?

```text
GET    /designtask/task/list
GET    /designtask/task/{taskId}
POST   /designtask/task
PUT    /designtask/task
DELETE /designtask/task/{taskIds}
POST   /designtask/task/{taskId}/submit
GET    /designtask/task/{taskId}/logs
GET    /designtask/task/types
PUT    /designtask/task/{taskId}/cancel
PUT    /designtask/task/node/{nodeId}/complete
GET    /designtask/flow/template/list
GET    /designtask/flow/node/list/{templateId}
GET    /designtask/flow/task/node/list/{taskId}
POST   /designtask/flow/task/node/{nodeId}/submit
```

### 4.2 鐠佹崘顓告禒璇插娑撳簼绱崠鏍ㄧウ缁?

```text
GET  /designtask/dashboard
GET  /designtask/process/definitions
POST /designtask/process/deploy-default
GET  /designtask/process/definition/{processDefinitionId}/nodes
GET  /designtask/assignee-options
POST /designtask/task/start
POST /designtask/task/upload-attachment
GET  /designtask/task/attachment/{fileId}
GET  /designtask/task/detail/{taskId}
GET  /designtask/task/{taskId}/archive
GET  /designtask/objective/catalog/{discipline}
GET  /designtask/design-variable/catalog/{discipline}
GET  /designtask/fault-pipe-parameters/default
GET  /designtask/fault-pipe-parameters/options
GET  /designtask/task/{taskId}/fault-pipe-parameters
POST /designtask/task/{taskId}/objective-constraints
POST /designtask/task/{taskId}/objective-weights
POST /designtask/task/{taskId}/design-variables
POST /designtask/task/{taskId}/conflict-check
POST /designtask/task/{taskId}/decompose
POST /designtask/task/{taskId}/solve
POST /designtask/task/{taskId}/surrogate-solve
GET  /designtask/task/{taskId}/surrogate-solve
POST /designtask/task/{taskId}/surrogate-solve/confirm
POST /designtask/task/{taskId}/simulation
POST /designtask/task/{taskId}/approve
```

`POST /designtask/task/start` 韫囧懘銆忔导鐘插弳閺堫剚顐兼禒璇插闁瀚ㄩ惃鍕吀濞堥潧寮弫浼存肠閿?

```json
{
  "faultPipeParameterSetId": 1
}
```

閸氬海顏导姘殺閸欘垰顦查悽銊ュ棘閺佷即娉︽径宥呭煑娑撹桨鎹㈤崝鈥虫彥閻撗嶇礉閸氬海鐢?CAD閵嗕竸NSYS閵嗕焦瀵氶弽鍥ь嚠濮ｆ柨娼庣拠璇插絿鐠囥儰鎹㈤崝锛勭拨鐎规艾寮弫鑸偓?

`POST /designtask/task/{taskId}/objective-weights` 閻劋绨拹鐔荤煑娴滃搫婀Ο鈥崇€风憴锝堚偓锔界湴鐟欙綁銆夌紒鐔剁娣囨繂鐡ㄩ惄顔界垼閺夊啴鍣搁敍?

```json
{
  "items": [
    { "discipline": "hydraulic", "itemCode": "HYD_STRESS_MIN", "weight": 6 },
    { "discipline": "hydraulic", "itemCode": "HYD_DEFORMATION_MIN", "weight": 4 }
  ]
}
```

閺夊啴鍣搁懠鍐ㄦ纯娑?`0-10`閵嗗倸鎮楃粩顖氬悑鐎硅妫惃?`0-100` 閺佺増宓侀敍宀冾嚢閸欐牗妞傛导姘床缁犳鐫嶇粈鎭掆偓?

`POST /designtask/task/{taskId}/simulation` 閻滄澘婀箛鍛淬€忛崠鍛儓娴滃搫浼愭宀冪槈缂佹捁顔戦敍?

```json
{
  "simulationPassed": true
}
```

婵″倹鐏夌紓鍝勭毌 `simulationPassed`閿涘苯鎮楃粩顖氱安閹锋帞绮烽幓鎰唉閿涘矂浼╅崗宥呭煕閺傜増瀵氶弽鍥ㄦ鐠囶垰鐣幋鎰ウ缁嬪鈧倷鎹㈤崝陇顕涢幆鍛复閸欙絼绱版潻鏂挎礀 `simulation.verified`閵嗕梗simulation.passed` 閸?`canConfirmSimulation`閵?

### 4.3 CAD 娑?ANSYS

```text
POST /designtask/task/{taskId}/cad-model
GET  /designtask/task/{taskId}/cad-model
GET  /designtask/task/{taskId}/cad-model/file/{kind}
POST /designtask/task/{taskId}/ansys-simulation
GET  /designtask/task/{taskId}/ansys-simulation
GET  /designtask/task/{taskId}/ansys-simulation/image
```

ANSYS 閹恒儱褰涢弨顖涘瘮 `simulationMode`閿?

```text
DEMO_SIMULATION_MODEL
BIDIRECTIONAL_FSI_MODEL
```

閸ュ墽澧栭幒銉ュ經闁俺绻冮崥搴ｎ伂鐠囪褰囬張顒佹簚閸ュ墽澧栭弬鍥︽鏉╂柨娲栫紒娆愮セ鐟欏牆娅掗敍灞肩瑝閻╁瓨甯撮弳鎾苟閺堝秴濮熼崳銊ь梿閻╂鐭惧鍕┾偓?

### 4.4 濡楀棙顣︾憗鍌滄睏鐎靛灝鎳?

```text
GET  /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-load-spectrum
GET  /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-maintenance-advice/confirm
```

### 4.5 閺嶅洤鍣挧鍕爱閼挎粌宕?

娴狅絿鐖滄稉顓炲讲娴犮儰绻氶悾娆掔カ濠ф劖甯撮崣锝忕礉娴ｅ棗缍嬮崜宥勭瑹閸斺€茬瑝闂団偓鐟曚讲鈧粍鐖ｉ崙鍡氱カ濠ф劗顓搁悶鍡忊偓婵婂綅閸楁洏鈧倹鏆ｉ崥鍫ｅ壖閺堫兛绱扮粔濠氭珟閼挎粌宕熼崗銉ュ經閸滃苯顕惔鏃囧綅閸楁洘娼堥梽鎰剁礉娑撳秴鍨归梽銈堢カ濠ф劘銆冮妴?

## 5. Python 閸?Worker 閺堝秴濮熼柊宥囩枂

### 5.1 娴狅絿鎮婂Ο鈥崇€烽張宥呭

閻╊喖缍嶉敍?

```text
python/project2/
```

閹恒儱褰涢敍?

```text
GET  /health
GET  /api/models/active
POST /api/surrogate/optimize
```

姒涙顓荤粩顖氬經閿?

```text
9721
```

閺堫剚婧€閸欘垯濞囬悽銊ュ嚒閺?conda 閻滎垰顣ㄩ崥顖氬З閿?

```powershell
cd python/project2
conda activate project2-fastapi
python -m uvicorn app.main:app --host 127.0.0.1 --port 9721
```

婵″倹鐏夐張宥呭閸ｃ劍鐥呴張澶庮嚉閻滎垰顣ㄩ敍宀勬付鐟曚礁鍨卞鐑樻煀閻滎垰顣ㄩ獮璺虹暔鐟佸懍绶风挧鏍电窗

```powershell
cd python/project2
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 9721
```

Java 闁板秶鐤嗛敍?

```properties
design.solver.surrogate-base-url=http://<娴狅絿鎮婂Ο鈥崇€烽張宥呭閸ｂ問P>:9721
```

閸嬨儱鎮嶅Λ鈧弻銉窗

```text
http://<娴狅絿鎮婂Ο鈥崇€烽張宥呭閸ｂ問P>:9721/health
```

### 5.2 濡楀棙顣︾憗鍌滄睏鐎靛灝鎳￠張宥呭

閻╊喖缍嶉敍?

```text
python/project22/
```

閹恒儱褰涢敍?

```text
GET  /health
POST /api/frame-beam-crack/growth-predict
```

姒涙顓荤粩顖氬經閿?

```text
9822
```

閸氼垰濮╅敍?

```powershell
cd python/project22
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 9822
```

Java 闁板秶鐤嗛敍?

```properties
designtask.frame-beam.surrogate-base-url=http://<濡楀棙顣﹂張宥呭IP>:9822
```

### 5.3 SolidWorks Worker

閻╊喖缍嶉敍?

```text
solidworks_worker/
```

閹恒儱褰涢敍?

```text
POST /api/pipe-model
```

姒涙顓婚崷鏉挎絻閿?

```text
http://127.0.0.1:18080/api/pipe-model
```

Java 闁板秶鐤嗛敍?

```properties
designtask.pipe-worker-base-url=http://<SolidWorks閺堝秴濮熼崳鈫朠>:18080
```

閺堝秴濮熼崳銊洣濮瑰偊绱?

- Windows 閻滎垰顣ㄩ妴?
- 瀹告彃鐣ㄧ憗?SolidWorks閿涘矁顔忛崣顖濈槈閸欘垳鏁ら妴?
- 閸氼垰濮?Worker 閻ㄥ嫮鏁ら幋鐤厴濮濓絽鐖堕幍鎾崇磻 SolidWorks閵?
- 娑撳秴缂撶拋顔荤稊娑撶儤妫ゅ宀勬桨缁崵绮洪張宥呭閸氼垰濮╅敍灞惧腹閼芥劒濞囬悽銊ュ讲娴溿倓绨板宀勬桨閻劍鍩涢妴?

SolidWorks Worker 韫囧懘銆忛悽鐔稿灇閸欏瞼顏拹顖炩偓姘扁敄韫囧啰顓搁敍宀冪翻閸戣櫣娈?`pipe_native.SLDPRT`閵嗕梗pipe_model.step`閵嗕梗pipe_model.x_t` 娑撳秴鍘戠拋鍛婃Ц娑撯偓缁旑垰鐨濋梻顓熷灗鐎圭偛绺惧Ο鈥崇€烽妴?

### 5.4 ANSYS Worker

閻╊喖缍嶉敍?

```text
ansys_worker/
```

閹恒儱褰涢敍?

```text
GET  /api/ansys/health
POST /api/ansys/import-geometry
```

姒涙顓婚崷鏉挎絻閿?

```text
http://127.0.0.1:18081/api/ansys/import-geometry
```

Java 闁板秶鐤嗛敍?

```properties
designtask.ansys-worker-base-url=http://<ANSYS閺堝秴濮熼崳鈫朠>:18081
```

閸忔娊鏁悳顖氼暔閸欐﹢鍣洪敍?

```bat
set "ANSYS_WORKBENCH_CMD=C:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat"
set "ANSYS_WORKER_HOST=0.0.0.0"
set "ANSYS_WORKER_PORT=18081"
set "ANSYS_MESH_SIZE_MM=3"
set "ANSYS_KEEP_MECHANICAL_OPEN=0"
set "ANSYS_MECHANICAL_INTERACTIVE=0"
set "ANSYS_MECHANICAL_RESULT_TIMEOUT=540"
set "ANSYS_IMAGE_EXPORT_WIDTH=1920"
set "ANSYS_IMAGE_EXPORT_HEIGHT=1080"
```

濞夈劍鍓伴敍?

- `ANSYS_WORKBENCH_CMD` 韫囧懘銆忛弨瑙勫灇閺堝秴濮熼崳銊ф埂鐎圭偠鐭惧鍕┾偓?
- 娑撳秷顩﹂柊宥囩枂瀵偓婵褰嶉崡?`.lnk` 韫囶偅宓庨弬鐟扮础閵?
- 閹恒劏宕橀柊宥囩枂 `runwb2.bat`閿涘奔绗夌憰浣风喘閸忓牓鍘ょ純?`RunWB2.exe`閵?
- 閻楀牊婀伴惄顔肩秿婵?`v221` 鐟曚焦瀵滈張宥呭閸ｃ劌鐣ㄧ憗鍛閺堫剝鐨熼弫娣偓?

## 6. 閺佺増宓佹惔鎾圭讣缁?

濮濓絽绱℃潻浣盒╃拠楣冾暯娴滃奔绗熼崝鈥崇氨閺冭泛褰ч幍褑顢戦敍?

```text
sql/t2_project2_full_migration.sql
```

閹笛嗩攽缁€杞扮伐閿?

```powershell
mysql -h <閺佺増宓佹惔鎾虫勾閸р偓> -P <缁旑垰褰? -u <閻劍鍩涢崥? -p <閺佺増宓佹惔鎾虫倳> < sql/t2_project2_full_migration.sql
```

鐠囥儲鏆ｉ崥鍫ｅ壖閺堫剙瀵橀崥顐窗

- 鐠囬箖顣芥禍?`t2_*` 鐞涖劎绮ㄩ弸鍕┾偓?
- 閺冄嗐€?`design_*` / `p2_*` 閸?`t2_*` 閻ㄥ嫬鍚嬬€瑰綊鍣搁崨钘夋倳閵?
- 閸忕厧顔愰崡鍥╅獓鐎涙顔岄妴?
- 閼挎粌宕熼妴浣筋潡閼瑰弶娼堥梽鎰┾偓浣稿灥婵瀵查弫鐗堝祦閵?
- 閺佸懘娈扮粻鈩冾唽閸欏倹鏆熼梿鍡愨偓?
- 缁犫剝顔岀紓鏍у娇閺嶅洤鍣崠鏍电窗`HP-PIPE-SEG-001`閵?
- 娴兼ê瀵查崜宥堫啎鐠佲€冲綁闁插繐鐔€閸戝棗鈧厧娲栨繅顐礉閸栧懏瀚鍙夋箒娴犺濮熻箛顐ゅ弾閵?
- ANSYS 閸欏本膩閸ㄥ绮ㄩ弸婊嗐€冮崡鍥╅獓閿涘畭t2_design_ansys_simulation_task` 娴ｈ法鏁?`(task_id, simulation_mode)` 閸栧搫鍨庣紒鎾寸亯閵?
- 濡楀棙顣︾憗鍌滄睏鐎靛灝鎳＄悰銊ユ嫲閼挎粌宕熼妴?
- 缁夊娅庨垾婊勭垼閸戝棜绁┃鎰吀閻炲棌鈧繆褰嶉崡鏇樷偓?

娑撳秷顩﹂崷銊ユ倱娑撯偓娑擃亞娲伴弽鍥х氨娑擃參鍣告径宥嗗⒔鐞涘苯鍙炬禒?`sql/t2_*.sql` 闂嗚埖鏆庨懘姘拱閵嗕繖sql/t2_fault_pipe_segment_code_update.sql`閵嗕梗sql/t2_fault_pipe_design_variable_baseline.sql` 缁涘鍑＄紒蹇撹嫙閸忋儱鐣弫纾嬬讣缁夋槒鍓奸張顒婄礉閸欘亜婀仦鈧柈銊ㄋ夋稉浣告簚閺咁垯绗呴崡鏇犲娴ｈ法鏁ら妴?

閻楄鐣╅懘姘拱鐠囧瓨妲戦敍?

- `flowable閻╃鍙х悰?sql`閿涙艾瀵橀崥?Flowable 瀵洘鎼哥悰銊ユ嫲 `DROP TABLE`閿涘苯褰ч崷銊ュ叡閸戔偓 Flowable 鎼存挻鍨ㄩ弰搴ｂ€橀棁鈧憰渚€鍣稿?Flowable 瀵洘鎼哥悰銊︽閸楁洜瀚幍褑顢戦妴?
- `flowable缂冩垵鍙х捄顖滄暠.sql`閿涙艾顩ч弸?master 閻ㄥ嫮缍夐崗瀹犵熅閻㈣京鏁遍弫鐗堝祦鎼?Nacos 缁狅紕鎮婇敍宀勬付鐟曚焦瀵滈惄顔界垼閻滎垰顣ㄩ崡鏇犲閸氬牆鍙嗛妴?
- `nacos闁板秶鐤?sql`閿涙艾鐫樻禍?Nacos 闁板秶鐤嗘惔鎿勭礉娑撳秷鍏橀惄瀛樺复瑜版挷绗熼崝鈥崇氨閼存碍婀伴幍褑顢戦妴?

閻╊喗鐖ｆ惔鎾诡洣濮瑰偊绱?

- 瀹稿弶婀?RuoYi-Cloud 閸╄櫣顢呯悰顭掔礉娓氬顩?`sys_menu`閵嗕梗sys_role`閵嗕梗sys_role_menu`閵?
- 瀹稿弶婀侀崺铏诡攨鐟欐帟澹婇弫鐗堝祦閿涘苯鎯侀崚娆掑綅閸楁洘宸块弶鍐嚔閸欍儲妫ゅ▔鏇氶獓閻㈢喖顣╅張鐔告櫏閺嬫嚎鈧?
- 閹笛嗩攽閸撳秴绻€妞よ顦禒濮愨偓?

## 7. Nacos閵嗕胶缍夐崗鍐叉嫲閺堝秴濮熼崳銊ユ勾閸р偓

### 7.1 ruoyi-designtask1 闁板秶鐤?

閺堝秴濮熼崥宥呮嫲缁旑垰褰涘楦款唴閿?

```yaml
server:
  port: 9801
spring:
  application:
    name: ruoyi-designtask1
```

Nacos 娑擃參娓剁憰浣规煀婢х偞鍨ㄩ崥鍫濊嫙閿?

```text
ruoyi-designtask1-dev.yml
```

閼峰啿鐨崠鍛儓閿?

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://<閺佺増宓佹惔鎻慞>:<缁旑垰褰?/<閺佺増宓佹惔鎾虫倳>
          username: <閻劍鍩涢崥?
          password: <鐎靛棛鐖?

design:
  solver:
    surrogate-base-url: http://<娴狅絿鎮婂Ο鈥崇€烽張宥呭IP>:9721

designtask:
  frame-beam:
    surrogate-base-url: http://<濡楀棙顣﹂張宥呭IP>:9822
  pipe-worker-base-url: http://<SolidWorks閺堝秴濮熼崳鈫朠>:18080
  ansys-worker-base-url: http://<ANSYS閺堝秴濮熼崳鈫朠>:18081
```

閸忚渹缍嬮柊宥囩枂闁款喕浜?Java 娴狅絿鐖滅拠璇插絿娑撳搫鍣敍灞炬殻閸氬牊妞傞幖婊呭偍閿?

```text
surrogate-base-url
pipe-worker
ansys-worker
```

### 7.2 缂冩垵鍙х捄顖滄暠

缂冩垵鍙ч棁鈧憰浣藉厴鏉烆剙褰傞敍?

```text
Path=/designtask/**
Service=ruoyi-designtask1
```

婵″倹鐏夋穱婵堟殌閺冄勫复閸欙絽鍩嗛崥?`/task/**`閵嗕梗/flow/**`閵嗕梗/template/**`閿涘奔绡冪憰浣衡€樼拋銈嗘Ц閸氾箒娴嗛崣鎴濆煂 `ruoyi-designtask1`閵嗗倸澧犵粩顖欏瘜鐟曚椒濞囬悽?`/designtask/**`閵?

### 7.3 閸ュ搫鐣?IP

娴犮儰绗呴柊宥囩枂娑撳秷鍏橀崷銊︽箛閸斺€虫珤娑撳﹣绻氶悾娆愭拱閺堝搫鈧》绱?

```yaml
spring:
  cloud:
    nacos:
      discovery:
        ip: 127.0.0.1
```

闁劎璁查崚鐗堟箛閸斺€虫珤閸氬函绱濇惔鏃€鏁兼稉鐑樻箛閸斺€虫珤閸愬懐缍?IP閿涘本鍨ㄩ崚鐘绘珟閸ュ搫鐣?IP 鐠?Nacos 閼奉亜濮╃拠鍡楀焼閵?

## 8. 閸氬牆鑻熼崥搴℃儙閸斻劑銆庢惔?

閹恒劏宕樻い鍝勭碍閿?

1. 婢跺洣鍞ら惄顔界垼閺佺増宓佹惔鎾扁偓?
2. 閹笛嗩攽 `sql/t2_project2_full_migration.sql`閵?
3. 閸氬牆鑻?Nacos 闁板秶鐤嗛崪宀€缍夐崗瀹犵熅閻究鈧?
4. 閸氼垰濮?Nacos閵嗕阜edis閵嗕府ySQL閵?
5. 閸氼垰濮?RuoYi 閸╄櫣顢呴張宥呭閿涙ateway閵嗕工uth閵嗕够ystem閵?
6. 閸氼垰濮?`ruoyi-designtask1`閵?
7. 閸氼垰濮?`python/project2` 娴狅絿鎮婂Ο鈥崇€烽張宥呭閵?
8. 閸氼垰濮?`python/project22` 濡楀棙顣︾憗鍌滄睏鐎靛灝鎳￠張宥呭閵?
9. 閸氼垰濮?`solidworks_worker`閵?
10. 閸氼垰濮?`ansys_worker`閵?
11. 閸氼垰濮╅幋鏍櫢閺備即鍎寸純鎻掑缁旑垬鈧?

缂傛牞鐦уΛ鈧弻銉窗

```powershell
mvn clean package -DskipTests
```

閸撳秶顏Λ鈧弻銉窗

```powershell
cd ruoyi-ui
npm install
npm run build:prod
```

## 9. 閸氬牆鑻熸灞炬暪濞撳懎宕?

### 9.1 閺傚洣娆㈠Λ鈧弻?

master 娑擃厼绨茬€涙ê婀敍?

```text
ruoyi-modules/ruoyi-designtask1/
ruoyi-ui/src/views/designtask/
ruoyi-ui/src/api/designtask/
python/project2/
python/project22/
solidworks_pipe/
solidworks_worker/
ansys_worker/
sql/t2_project2_full_migration.sql
TEAM_INTEGRATION_README.md
project2_readme.md
```

master 娑擃厺绗夋惔鏂垮瘶閸氼偓绱?

```text
ansys_worker/output/
solidworks_worker/output/
tmp_wbpz_compare_1/
solidworks_pipe/params.json
```

### 9.2 閺堝秴濮熼崑銉ユ倣濡偓閺?

```text
GET http://<娴狅絿鎮婂Ο鈥崇€烽張宥呭IP>:9721/health
GET http://<濡楀棙顣﹂張宥呭IP>:9822/health
GET http://<ANSYS閺堝秴濮熼崳鈫朠>:18081/api/ansys/health
```

SolidWorks Worker 闁俺绻冮獮鍐插酱閻㈢喐鍨?CAD 濡€崇€锋宀冪槈閵?

### 9.3 娑撴艾濮熼柧鎹愮熅妤犲本鏁?

閼峰啿鐨捄鎴︹偓姘剧窗

```text
閸掓稑缂撶拠楣冾暯娴滃奔鎹㈤崝?
闁瀚ㄧ粻鈩冾唽缂傛牕褰?
閸氬嫪绗撴稉姘垛偓澶嬪閻╊喗鐖?缁撅附娼?
鐠愮喕鐭楁禍鐑樺⒔鐞涘瞼娲伴弽鍥╁閺夌喐鐗庢?
鐠愮喕鐭楁禍鍝勭秺閸欙綀顔曠純顔炬窗閺嶅洦娼堥柌?
閹笛嗩攽娴狅絿鎮婂Ο鈥崇€峰Ч鍌澬?
閻㈢喐鍨?SolidWorks CAD
閸氼垰濮?ANSYS 濠曟梻銇氭禒璺ㄦ埂濡€崇€?
閺屻儳婀呮惔鏂垮閸ユ儳鎷伴惇鐔风杽缂佹挻鐏夐幐鍥ㄧ垼
瀹搞儳鈻肩敮鍫熷閸斻劍褰佹禍銈勮雹閻喖鐛欑拠渚€鈧俺绻?娑撳秹鈧俺绻?
妫板棗顕辩€光剝澹掓稉搴濇崲閸斺€崇秺濡?
```

濡楀棙顣︾憗鍌滄睏闁炬崘鐭鹃崡鏇犲妤犲矁鐦夐敍?

```text
瑜版洖鍙嗙憗鍌滄睏閸欏倹鏆?
娑撳﹣绱堕幋鏍偓澶嬪鏉炲€熷祹鐠?
鐠嬪啰鏁?9822 鐎靛灝鎳℃０鍕ゴ閺堝秴濮?
閻㈢喐鍨氱紒缈犳叏/閸愬磭鐡ュ楦款唴
绾喛顓诲楦款唴
```

### 9.4 妞ょ敻娼版灞炬暪

闁插秶鍋ｅΛ鈧弻銉窗

- 妫ｆ牠銆夐妴浣稿礂閸氬本婧€閸掕翰鈧胶娲伴弽鍥偓澶嬪閵嗕焦膩閸ㄥ袙閼帮负鈧椒璞㈤惇鐔肩崣鐠囦降鈧礁缍婂锝夈€夐棃銏ゎ棑閺嶈偐绮烘稉鈧妴?
- 閸楀繐鎮撻張鍝勫煑閻㈢喐鍨氭い鍨箒缁犫剝顔岀紓鏍у娇闁瀚ㄩ妴?
- 閻╊喗鐖ｇ痪锔芥将闁瀚ㄦい鍏哥瑝閸愬秴鐫嶇粈鐑樻綀闁插秵绮﹂弶掳鈧?
- 濡€崇€风憴锝堚偓锕傘€夊锔挎櫠閻╊喗鐖ｉ妴浣稿礁娓氀呭閺夌喎鍨庨弽蹇撶潔缁€鐚寸礉閺夊啴鍣告稉?`0-10`閵?
- 閺嶏繝鐛欑紒鎾寸亯閸欘亜婀悙鐟板毊閹笛嗩攽閺嶏繝鐛欓崥搴″毉閻滆埇鈧?
- 娴犺法婀℃宀冪槈妞ら潧褰ч弰鍓с仛鐠佹崘顓搁崣姗€鍣虹€佃鐦妴浣规付婢堆呯搼閺佸牆绨查崝娑栤偓浣规付婢堆勨偓璇插綁瑜邦潿鈧?
- 妤犲矁鐦夌紒鎾诡啈韫囧懘銆忔禍鍝勪紣闁瀚ㄩ崥搴㈠閼宠姤褰佹禍銈冣偓?
- 缁狅紕鎮婇崨妯诲灗瑜版挸澧犻崣顖氼槱閻炲棔姹夐崣顖涘絹娴溿倓璞㈤惇鐔虹波鐠佺尨绱濋崢鍡楀蕉娴犺濮熺悰銉ョ秿娑撳秳绱伴柌宥咁槻閹恒劌濮╁ù浣衡柤閵?

## 10. 娑撴槒顩︽搴ㄦ珦閻?

- 閺?`pom.xml` 閻楀牊婀伴崣妯绘纯娴兼艾濂栭崫宥呭弿妞ゅ湱娲伴敍灞芥値楠炶泛鎮楄箛鍛淬€忛崗銊ょ波鎼存挾绱拠鎴欌偓?
- `ruoyi-ui/src/router/index.js` 鐎硅妲楁稉搴″従娴犳牞顕虫０妯哄暱缁愪緤绱濇稉宥堝厴閻╁瓨甯寸憰鍡欐磰閵?
- Nacos 娑擃厽澧嶉張?`127.0.0.1` 閸掔増婀囬崝鈥虫珤閸氬酣鍏橀崣顖濆厴婢惰鲸鏅ラ敍宀勬付鐟曚線鈧劙銆嶉弴鎸庡床閵?
- Python 姒涙顓婚悳顖氼暔娑撳秳绔寸€规艾鐣ㄧ憗?FastAPI閵嗕腐umPy閵嗕讣ciPy 缁涘绶风挧鏍电幢娴狅絿鎮婂Ο鈥崇€锋稉宥呭讲閻劍妞傛导妯哄帥濡偓閺?`9721/health` 閸滃矁娅勯幏鐔哄箚婢у啨鈧?
- SolidWorks 閸?ANSYS 娓氭繆绂?Windows 鏉烆垯娆㈤妴浣筋啅閸欘垵鐦夐崪灞藉讲娴溿倓绨板宀勬桨閻滎垰顣ㄩ敍灞肩瑝闁倸鎮庨惄瀛樺复闁劎璁查崚鐗堟珮闁?Linux 閺堝秴濮熼懞鍌滃仯閵?
- ANSYS `BIDIRECTIONAL_FSI_MODEL` 瑜版挸澧犻悽銊ょ艾閻㈢喐鍨氶崣鍌濃偓鍐ㄤ紣缁嬪鎷伴柊宥囩枂閿涙稖瀚㈢憰浣规￥娴滃搫鈧厧鐣ч惇鐔风杽閸欏苯鎮滃ù浣告祼閼帮箑鎮庡Ч鍌澬掗敍宀冪箷闂団偓缂佈呯敾鐞涖儵缍?Fluent 濞翠椒缍嬮崺鐔粹偓浣告嚒閸氬秷绔熼悾灞烩偓浣虹秹閺嶇厧鎷?System Coupling 閼奉亜濮╅崠鏍壖閺堫兙鈧?
- 閺佺増宓佹惔鎾搭劀瀵繗绺肩粔璇插涧閹笛嗩攽 `sql/t2_project2_full_migration.sql`閿涘奔绗夌憰渚€鍣告径宥嗗⒔鐞涘矂娴傞弫?SQL閵?
