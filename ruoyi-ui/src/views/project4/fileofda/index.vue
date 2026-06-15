<template>
  <div class="project4-page">
    <!-- 椤甸潰鏍囬鍗＄墖 -->
    <section class="module-hero">
      <div>
        <div class="module-eyebrow">璇鹃鍥?路 鑸┖瑁呭璐ㄩ噺杩芥函</div>
        <h2>鏁版嵁鏂囦欢绠＄悊</h2>
        <p>
          绠＄悊鍘熷鏁版嵁鏂囦欢銆佹枃浠惰В鏋愮姸鎬併€佹牱鏈暟閲忎笌鏁版嵁褰掓。淇℃伅锛屼负鍚庣画鏍锋湰澧炲己銆佺壒寰佽瀺鍚堜笌鏁呴殰璇婃柇鎻愪緵鏁版嵁鍩虹銆?
        </p>
      </div>

      <div class="module-status">
        <span>鏁版嵁宸叉帴鍏?/span>
        <span>婕旂ず妯″紡</span>
      </div>
    </section>

    <!-- 鎸囨爣鍗＄墖 -->
    <section class="metric-strip">
      <div class="metric-mini">
        <span>鏂囦欢鎬绘暟</span>
        <strong>{{ total }}</strong>
        <em>褰撳墠鏁版嵁闆?/em>
      </div>

      <div class="metric-mini">
        <span>褰撳墠椤佃В鏋愭垚鍔?/span>
        <strong>{{ parseSuccessCount }}</strong>
        <em>parseStatus = 鎴愬姛</em>
      </div>

      <div class="metric-mini">
        <span>褰撳墠椤靛鍏ユ垚鍔?/span>
        <strong>{{ importSuccessCount }}</strong>
        <em>importStatus = 鎴愬姛</em>
      </div>

      <div class="metric-mini">
        <span>褰撳墠椤垫牱鏈暟閲?/span>
        <strong>{{ sampleTotal }}</strong>
        <em>sampleCount 姹囨€?/em>
      </div>
    </section>

    <!-- 鏁版嵁闆嗗鍏ヤ笌閫夋嫨 -->
    <section class="dataset-card">
      <div class="card-header">
        <div>
          <div class="module-eyebrow">鏁版嵁闆嗙鐞?/div>
          <h3>瀵煎叆鏁版嵁闆嗕笌閫夋嫨鏁版嵁闆?/h3>
          <p class="section-desc">
            鏀寔瀵煎叆鍘熷杞存壙鎸姩鏁版嵁闆嗘枃浠跺す鎴栧崟涓暟鎹枃浠讹紝骞堕€夋嫨褰撳墠鐢ㄤ簬棰勫鐞嗐€佹牱鏈垎甯у拰鍚庣画璇婃柇鍒嗘瀽鐨勬暟鎹泦銆?
          </p>
        </div>

        <el-tag type="primary" effect="plain">
          褰撳墠鏁版嵁闆嗭細{{ currentDataset?.datasetName || "鏈€夋嫨" }}
        </el-tag>
      </div>

      <div class="dataset-grid">
        <div class="dataset-panel">
          <div class="panel-title">瀵煎叆鏁版嵁闆嗘枃浠跺す</div>

          <div class="folder-upload-box">
            <div class="folder-upload-icon">
              <el-icon><FolderOpened /></el-icon>
            </div>

            <div class="folder-upload-title">
              閫夋嫨 CWRU 鏁版嵁闆嗘枃浠跺す鎴栧崟涓暟鎹枃浠?
            </div>

            <div class="folder-upload-desc">
              鎺ㄨ崘閫夋嫨鏁版嵁闆嗘牴鐩綍鎴栧瓙鐩綍锛岀郴缁熶細鑷姩璇嗗埆鍏朵腑鐨?MAT銆丆SV銆乀XT銆乆LSX 鏂囦欢锛涗篃鍙互閫夋嫨鍗曚釜鏂囦欢杩涜婕旂ず瀵煎叆銆?
            </div>

            <div class="folder-upload-actions">
              <el-button type="primary" icon="FolderOpened" @click="handleChooseDatasetFolder">
                閫夋嫨鏂囦欢澶?
              </el-button>
              <el-button plain icon="Document" @click="handleChooseSingleDatasetFile">
                閫夋嫨鍗曚釜鏂囦欢
              </el-button>
            </div>

            <input
                ref="datasetFolderInputRef"
                class="hidden-file-input"
                type="file"
                multiple
                webkitdirectory
                directory
                @change="handleDatasetFolderChange"
            />

            <input
                ref="datasetSingleFileInputRef"
                class="hidden-file-input"
                type="file"
                accept=".mat,.csv,.txt,.xlsx"
                @change="handleDatasetSingleFileChange"
            />

            <div v-if="datasetUploadFiles.length" class="selected-dataset-summary">
              <div>
                <span>{{ datasetUploadMode === "folder" ? "宸查€夋嫨鏂囦欢澶? : "宸查€夋嫨鏂囦欢" }}</span>
                <strong>{{ datasetUploadName }}</strong>
              </div>
              <el-tag type="success" effect="plain">鏈夋晥鏂囦欢 {{ datasetUploadFiles.length }} 涓?/el-tag>
            </div>
          </div>

          <div class="upload-actions">
            <el-button type="primary" icon="Upload" @click="handleImportDataset">
              瀵煎叆鏁版嵁闆?
            </el-button>
            <el-button icon="Refresh" @click="handleResetDatasetImport">
              娓呯┖
            </el-button>
          </div>
        </div>

        <div class="dataset-panel">
          <div class="panel-title">閫夋嫨鏁版嵁闆?/div>

          <el-form :model="datasetForm" label-width="110px">
            <el-form-item label="褰撳墠鏁版嵁闆?>
              <el-select
                  v-model="datasetForm.datasetId"
                  placeholder="璇烽€夋嫨鏁版嵁闆?
                  style="width: 100%"
                  @change="handleDatasetChange"
              >
                <el-option
                    v-for="item in datasetOptions"
                    :key="item.datasetId"
                    :label="item.datasetName"
                    :value="item.datasetId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="鏁版嵁闆嗙被鍨?>
              <el-input v-model="datasetForm.datasetType" disabled />
            </el-form-item>

            <el-form-item label="鏂囦欢鏁伴噺">
              <el-input v-model="datasetForm.fileCount" disabled />
            </el-form-item>

            <el-form-item label="鏍锋湰鎬婚噺">
              <el-input v-model="datasetForm.sampleCount" disabled />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </section>

    <!-- 鏁版嵁琛ㄦ牸 -->
    <section class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">鏁版嵁鍒楄〃</div>
          <h3>鍘熷鏁版嵁鏂囦欢</h3>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <el-collapse-transition>
        <div v-show="showSearch" class="raw-query-box">
          <div class="inline-section-title">鏁版嵁鏂囦欢鏌ヨ</div>
          <el-form
              :model="queryParams"
              ref="queryRef"
              :inline="true"
              class="raw-query-form"
              label-width="92px"
          >
            <el-form-item label="鏁版嵁闆咺D" prop="datasetId">
              <el-input-number
                  v-model="queryParams.datasetId"
                  :controls="false"
                  :min="0"
                  placeholder="璇疯緭鍏ユ暟鎹泦ID"
                  clearable
                  style="width: 170px"
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="鏂囦欢缂栧彿" prop="fileCode">
              <el-input
                  v-model="queryParams.fileCode"
                  placeholder="璇疯緭鍏ユ枃浠剁紪鍙?
                  clearable
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="鍘熷鏂囦欢鍚? prop="originalFileName">
              <el-input
                  v-model="queryParams.originalFileName"
                  placeholder="璇疯緭鍏ュ師濮嬫枃浠跺悕"
                  clearable
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="鏂囦欢鍚庣紑" prop="fileSuffix">
              <el-input
                  v-model="queryParams.fileSuffix"
                  placeholder="璇疯緭鍏ユ枃浠跺悗缂€"
                  clearable
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="瑙ｆ瀽鐘舵€? prop="parseStatus">
              <el-select
                  v-model="queryParams.parseStatus"
                  placeholder="璇烽€夋嫨瑙ｆ瀽鐘舵€?
                  clearable
                  style="width: 170px"
              >
                <el-option label="鎴愬姛" value="鎴愬姛" />
                <el-option label="澶辫触" value="澶辫触" />
                <el-option label="鏈В鏋? value="鏈В鏋? />
              </el-select>
            </el-form-item>

            <el-form-item class="query-actions">
              <el-button type="primary" icon="Search" @click="handleQuery">鎼滅储</el-button>
              <el-button icon="Refresh" @click="resetQuery">閲嶇疆</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['system:fileofda:add']"
          >
            鏂板
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['system:fileofda:edit']"
          >
            淇敼
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['system:fileofda:remove']"
          >
            鍒犻櫎
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
              v-hasPermi="['system:fileofda:export']"
          >
            瀵煎嚭
          </el-button>
        </el-col>
      </el-row>

      <el-table
          v-loading="loading"
          :data="fileofdaList"
          border
          stripe
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="鏂囦欢ID" align="center" prop="fileId" width="90" />
        <el-table-column label="鏁版嵁闆咺D" align="center" prop="datasetId" width="100" />
        <el-table-column label="鏂囦欢缂栧彿" align="center" prop="fileCode" width="150" show-overflow-tooltip />
        <el-table-column label="鍘熷鏂囦欢鍚? align="center" prop="originalFileName" width="180" show-overflow-tooltip />
        <el-table-column label="鍚庣紑" align="center" prop="fileSuffix" width="80" />
        <el-table-column label="绫诲瀷" align="center" prop="fileType" width="90" />

        <el-table-column label="鏂囦欢澶у皬" align="center" prop="fileSize" width="110">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>

        <el-table-column label="瀛樺偍璺緞" align="center" prop="storagePath" width="220" show-overflow-tooltip />
        <el-table-column label="鏂囦欢MD5" align="center" prop="fileMd5" width="180" show-overflow-tooltip />

        <el-table-column label="鏁版嵁鏉ユ簮" align="center" prop="sourceType" width="110">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.sourceType || "-" }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="瀵煎叆鐘舵€? align="center" prop="importStatus" width="110">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.importStatus)" effect="plain">
              {{ scope.row.importStatus || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="瑙ｆ瀽鐘舵€? align="center" prop="parseStatus" width="110">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.parseStatus)" effect="plain">
              {{ scope.row.parseStatus || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="鏍锋湰鏁伴噺" align="center" prop="sampleCount" width="110" />
        <el-table-column label="閿欒淇℃伅" align="center" prop="errorMsg" width="200" show-overflow-tooltip />
        <el-table-column label="澶囨敞" align="center" prop="remark" width="220" show-overflow-tooltip />

        <el-table-column label="鎿嶄綔" align="center" width="150" fixed="right">
          <template #default="scope">
            <el-button
                link
                type="primary"
                icon="Edit"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['system:fileofda:edit']"
            >
              淇敼
            </el-button>

            <el-button
                link
                type="primary"
                icon="Delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:fileofda:remove']"
            >
              鍒犻櫎
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />
    </section>


    <!-- 鏁版嵁棰勫鐞?-->
    <section class="preprocess-card">
      <div class="card-header">
        <div>
          <div class="module-eyebrow">鏁版嵁棰勫鐞?/div>
          <h3>鏁版嵁棰勫鐞嗛厤缃?/h3>
          <p class="section-desc">
            瀵瑰綋鍓嶆暟鎹泦鎵ц婊ゆ尝鍘诲櫔銆佸綊涓€鍖栦笌鏁版嵁鍒嗗抚锛屽叾涓暟鎹垎甯т负蹇呴€夋楠わ紱澶勭悊瀹屾垚鍚庣敓鎴愬彲鐢ㄤ簬鏍锋湰澧炲己銆佺壒寰佽瀺鍚堝拰鏁呴殰璇婃柇鐨勬牱鏈獥鍙ｃ€?
          </p>
        </div>

        <el-tag :type="preprocessStatus === '宸插畬鎴? ? 'success' : 'warning'" effect="plain">
          澶勭悊鐘舵€侊細{{ preprocessStatus }}
        </el-tag>
      </div>

      <el-form :model="preprocessForm" label-width="130px">
        <div class="preprocess-layout">
          <div class="preprocess-main">
            <div class="preprocess-panel preprocess-panel-compact">
              <div class="panel-title">棰勫鐞嗘搷浣?/div>

              <el-form-item label="鎿嶄綔閫夋嫨">
                <el-checkbox-group v-model="preprocessForm.steps">
                  <el-checkbox label="denoise">婊ゆ尝鍘诲櫔</el-checkbox>
                  <el-checkbox label="normalize">褰掍竴鍖?/el-checkbox>
                  <el-checkbox label="frame" disabled>鏁版嵁鍒嗗抚锛堝繀閫夛級</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item
                  v-if="preprocessForm.steps.includes('denoise')"
                  label="鍘诲櫔鏂规硶"
              >
                <el-select
                    v-model="preprocessForm.denoiseMethod"
                    placeholder="璇烽€夋嫨鍘诲櫔鏂规硶"
                    style="width: 100%"
                >
                  <el-option label="灏忔尝鍘诲櫔" value="wavelet" />
                  <el-option label="宸寸壒娌冩柉甯﹂€氭护娉? value="butterworth" />
                  <el-option label="甯屽皵浼壒榛勫彉鎹? value="hht" />
                </el-select>
              </el-form-item>

              <el-form-item
                  v-if="preprocessForm.steps.includes('normalize')"
                  label="褰掍竴鍖栨柟娉?
              >
                <el-select
                    v-model="preprocessForm.normalizeMethod"
                    placeholder="璇烽€夋嫨褰掍竴鍖栨柟娉?
                    style="width: 100%"
                >
                  <el-option label="Z-score 鏍囧噯鍖? value="zscore" />
                  <el-option label="Min-Max 褰掍竴鍖? value="minmax" />
                </el-select>
              </el-form-item>
            </div>

            <div class="preprocess-panel preprocess-panel-compact">
              <div class="panel-title">鏁版嵁鍒嗗抚鍙傛暟</div>

              <div class="frame-param-grid">
                <el-form-item label="鏃堕棿绐楅暱搴?>
                  <el-input-number
                      v-model="preprocessForm.windowSize"
                      :min="128"
                      :step="128"
                      style="width: 100%"
                  />
                </el-form-item>

                <el-form-item label="姝ラ暱璁惧畾">
                  <el-input-number
                      v-model="preprocessForm.stride"
                      :min="1"
                      :step="64"
                      style="width: 100%"
                  />
                </el-form-item>
              </div>

              <el-form-item label="閲嶅彔鐜?>
                <el-slider
                    v-model="preprocessForm.overlapRate"
                    :min="0"
                    :max="90"
                    :step="5"
                    show-input
                />
              </el-form-item>
            </div>
          </div>

          <div class="preprocess-panel preprocess-summary">
            <div class="panel-title">澶勭悊鎽樿</div>

            <div class="summary-item">
              <span>褰撳墠鏁版嵁闆?/span>
              <strong>{{ currentDataset?.datasetName || "-" }}</strong>
            </div>

            <div class="summary-item">
              <span>澶勭悊娴佺▼</span>
              <strong>{{ buildPreprocessFlowText() }}</strong>
            </div>

            <div class="summary-item">
              <span>棰勮鐢熸垚鏍锋湰鏁?/span>
              <strong>{{ estimatedFrameCount }}</strong>
            </div>

            <el-button
                type="primary"
                icon="Operation"
                style="width: 100%; margin-top: 10px"
                @click="handleRunPreprocess"
            >
              鎵ц鏁版嵁棰勫鐞?
            </el-button>
          </div>
        </div>
      </el-form>
    </section>

    <!-- 棰勫鐞嗗悗鏍锋湰鍒楄〃 -->
    <section class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">澶勭悊缁撴灉</div>
          <h3>棰勫鐞嗗悗鏁版嵁鏍锋湰鍒楄〃</h3>
          <p class="section-desc">
            灞曠ず瀹屾垚鍘诲櫔銆佸綊涓€鍖栧拰鏁版嵁鍒嗗抚鍚庣殑鏍锋湰绐楀彛锛岀敤浜庡悗缁牱鏈寮恒€佺壒寰佽瀺鍚堝拰鏁呴殰璇婃柇銆?
          </p>
        </div>

        <div class="processed-table-actions">
          <el-button
              class="processed-action-btn"
              type="success"
              plain
              icon="Right"
              :disabled="!canGoAugment"
              @click="handleGoAugment"
          >
            杩涘叆鏍锋湰澧炲己
          </el-button>

          <el-button
              class="processed-action-btn"
              type="primary"
              plain
              icon="Download"
              @click="handleExportProcessedSamples"
          >
            瀵煎嚭鏍锋湰鍒楄〃
          </el-button>
        </div>
      </div>

      <el-table
          :data="processedSampleList"
          border
          stripe
          empty-text="璇峰厛閫夋嫨鏁版嵁闆嗗苟鎵ц鏁版嵁棰勫鐞?
      >
        <el-table-column label="鏍锋湰ID" align="center" prop="sampleId" width="90" />
        <el-table-column label="鏍锋湰缂栧彿" align="center" prop="sampleCode" width="150" />
        <el-table-column label="鏉ユ簮鏂囦欢" align="center" prop="sourceFileName" width="220" show-overflow-tooltip />
        <el-table-column label="鏃堕棿绐楅暱搴? align="center" prop="windowSize" width="120" />
        <el-table-column label="姝ラ暱" align="center" prop="stride" width="90" />
        <el-table-column label="閲嶅彔鐜? align="center" prop="overlapRate" width="100">
          <template #default="scope">
            {{ scope.row.overlapRate }}%
          </template>
        </el-table-column>
        <el-table-column label="鍘诲櫔鏂规硶" align="center" prop="denoiseMethod" width="170" />
        <el-table-column label="褰掍竴鍖栨柟娉? align="center" prop="normalizeMethod" width="170" />
        <el-table-column label="鏍锋湰缁村害" align="center" prop="sampleShape" width="120" />
        <el-table-column label="澶勭悊鐘舵€? align="center" prop="status" width="110">
          <template #default="scope">
            <el-tag type="success" effect="plain">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="鐢熸垚鏃堕棿" align="center" prop="createTime" width="170" />
      </el-table>
    </section>

    <!-- 鏂板 / 淇敼寮圭獥 -->
    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="fileofdaRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="鏁版嵁闆咺D" prop="datasetId">
              <el-input-number
                  v-model="form.datasetId"
                  :controls="false"
                  :min="0"
                  placeholder="璇疯緭鍏ユ暟鎹泦ID"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏂囦欢缂栧彿" prop="fileCode">
              <el-input v-model="form.fileCode" placeholder="璇疯緭鍏ユ枃浠剁紪鍙? />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鍘熷鏂囦欢鍚? prop="originalFileName">
              <el-input v-model="form.originalFileName" placeholder="璇疯緭鍏ュ師濮嬫枃浠跺悕" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏂囦欢鍚庣紑" prop="fileSuffix">
              <el-input v-model="form.fileSuffix" placeholder="璇疯緭鍏ユ枃浠跺悗缂€" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏂囦欢绫诲瀷" prop="fileType">
              <el-input v-model="form.fileType" placeholder="渚嬪 mat / csv / txt" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏂囦欢澶у皬" prop="fileSize">
              <el-input-number
                  v-model="form.fileSize"
                  :controls="false"
                  :min="0"
                  placeholder="璇疯緭鍏ユ枃浠跺ぇ灏忥紝鍗曚綅瀛楄妭"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏁版嵁鏉ユ簮" prop="sourceType">
              <el-select v-model="form.sourceType" placeholder="璇烽€夋嫨鏁版嵁鏉ユ簮" style="width: 100%">
                <el-option label="CWRU" value="CWRU" />
                <el-option label="鏂囦欢涓婁紶" value="鏂囦欢涓婁紶" />
                <el-option label="浼犳劅鍣ㄤ笂浼? value="浼犳劅鍣ㄤ笂浼? />
                <el-option label="缁翠慨璁板綍" value="缁翠慨璁板綍" />
                <el-option label="椋炶鏃ュ織" value="椋炶鏃ュ織" />
                <el-option label="鎵嬪伐褰曞叆" value="鎵嬪伐褰曞叆" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏍锋湰鏁伴噺" prop="sampleCount">
              <el-input-number
                  v-model="form.sampleCount"
                  :controls="false"
                  :min="0"
                  placeholder="璇疯緭鍏ユ牱鏈暟閲?
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="瀵煎叆鐘舵€? prop="importStatus">
              <el-select v-model="form.importStatus" placeholder="璇烽€夋嫨瀵煎叆鐘舵€? style="width: 100%">
                <el-option label="鎴愬姛" value="鎴愬姛" />
                <el-option label="澶辫触" value="澶辫触" />
                <el-option label="鏈鍏? value="鏈鍏? />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="瑙ｆ瀽鐘舵€? prop="parseStatus">
              <el-select v-model="form.parseStatus" placeholder="璇烽€夋嫨瑙ｆ瀽鐘舵€? style="width: 100%">
                <el-option label="鎴愬姛" value="鎴愬姛" />
                <el-option label="澶辫触" value="澶辫触" />
                <el-option label="鏈В鏋? value="鏈В鏋? />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="鏂囦欢瀛樺偍璺緞" prop="storagePath">
              <el-input v-model="form.storagePath" placeholder="璇疯緭鍏ユ枃浠跺瓨鍌ㄨ矾寰? />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="鏂囦欢MD5" prop="fileMd5">
              <el-input v-model="form.fileMd5" placeholder="璇疯緭鍏ユ枃浠禡D5" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="閿欒淇℃伅" prop="errorMsg">
              <el-input v-model="form.errorMsg" type="textarea" placeholder="璇疯緭鍏ラ敊璇俊鎭? />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="澶囨敞" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="璇疯緭鍏ュ娉? />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">纭?瀹?/el-button>
          <el-button @click="cancel">鍙?娑?/el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Fileofda">
import { computed, getCurrentInstance, reactive, ref, toRefs } from "vue"
import { FolderOpened } from "@element-plus/icons-vue"
import {
  createTopic4Pipeline,
  updateTopic4Pipeline
} from "@/utils/project4/topic4Pipeline"
import {
  listFileofda,
  getFileofda,
  delFileofda,
  addFileofda,
  updateFileofda,
  executePreprocess,
  listRawSamples
} from "@/api/project4/fileofda"

const { proxy } = getCurrentInstance()

const fileofdaList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const datasetUploadFiles = ref([])
const datasetUploadMode = ref("")
const datasetUploadName = ref("")
const datasetFolderInputRef = ref(null)
const datasetSingleFileInputRef = ref(null)
const preprocessStatus = ref("寰呭鐞?)
const processedSampleList = ref([])
const currentPipelineId = ref(null)
const canGoAugment = ref(false)

const datasetOptions = ref([
  {
    datasetId: 1,
    datasetName: "CWRU 杞存壙鏁呴殰鏁版嵁闆?,
    datasetType: "杞存壙鎸姩淇″彿",
    fileCount: 6,
    sampleCount: 12
  },
  {
    datasetId: 2,
    datasetName: "鑸┖瑁呭杞存壙璇曢獙鏁版嵁闆?,
    datasetType: "澶氫紶鎰熷櫒鏃跺簭鏁版嵁",
    fileCount: 8,
    sampleCount: 16
  }
])

const datasetForm = reactive({
  datasetId: 1,
  datasetName: "CWRU 杞存壙鏁呴殰鏁版嵁闆?,
  datasetType: "杞存壙鎸姩淇″彿",
  fileCount: 6,
  sampleCount: 12
})

const preprocessForm = reactive({
  steps: ["frame"],
  denoiseMethod: "wavelet",
  normalizeMethod: "zscore",
  windowSize: 1024,
  stride: 512,
  overlapRate: 50
})

const currentDataset = computed(() => {
  return datasetOptions.value.find(item => item.datasetId === datasetForm.datasetId)
})

const estimatedFrameCount = computed(() => {
  const dataset = currentDataset.value

  if (!dataset) {
    return 0
  }

  const baseCount = Number(dataset.sampleCount || 0)
  const overlapFactor = 1 + Number(preprocessForm.overlapRate || 0) / 100

  return Math.round(baseCount * overlapFactor * 8)
})


const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    datasetId: null,
    fileCode: null,
    originalFileName: null,
    fileSuffix: null,
    fileMd5: null,
    parseStatus: null
  },
  rules: {
    datasetId: [
      { required: true, message: "鏁版嵁闆咺D涓嶈兘涓虹┖", trigger: "blur" }
    ],
    fileCode: [
      { required: true, message: "鏂囦欢缂栧彿涓嶈兘涓虹┖", trigger: "blur" }
    ],
    originalFileName: [
      { required: true, message: "鍘熷鏂囦欢鍚嶄笉鑳戒负绌?, trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

const parseSuccessCount = computed(() => {
  return fileofdaList.value.filter(item => item.parseStatus === "鎴愬姛").length
})

const importSuccessCount = computed(() => {
  return fileofdaList.value.filter(item => item.importStatus === "鎴愬姛").length
})

const sampleTotal = computed(() => {
  return fileofdaList.value.reduce((sum, item) => {
    return sum + Number(item.sampleCount || 0)
  }, 0)
})

function getList() {
  loading.value = true
  listFileofda(queryParams.value).then(response => {
    fileofdaList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(error => {
    console.error("鏁版嵁鏂囦欢鏌ヨ澶辫触锛?, error)
    fileofdaList.value = []
    total.value = 0
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    fileId: null,
    datasetId: null,
    fileCode: null,
    originalFileName: null,
    fileSuffix: null,
    fileType: null,
    fileSize: 0,
    storagePath: null,
    fileMd5: null,
    sourceType: "CWRU",
    importStatus: "鎴愬姛",
    parseStatus: "鎴愬姛",
    sampleCount: 0,
    errorMsg: null,
    delFlag: "0",
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }

  proxy.resetForm("fileofdaRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

const datasetFilePattern = /\.(mat|csv|txt|xlsx)$/i

function handleChooseDatasetFolder() {
  datasetFolderInputRef.value?.click?.()
}

function handleChooseSingleDatasetFile() {
  datasetSingleFileInputRef.value?.click?.()
}

function getImportFileName(file) {
  const relativePath = file?.webkitRelativePath || ""
  const fileName = file?.name || ""

  if (relativePath) {
    return relativePath.split("/").pop() || fileName
  }

  return fileName
}

function getImportFileRelativePath(file) {
  return file?.webkitRelativePath || file?.name || ""
}

function getFolderNameFromFiles(files) {
  const firstPath = getImportFileRelativePath(files[0])

  if (firstPath.includes("/")) {
    return firstPath.split("/")[0]
  }

  return firstPath.replace(datasetFilePattern, "") || "瀵煎叆鏁版嵁闆?
}

function filterSupportedDatasetFiles(files) {
  return Array.from(files || []).filter(file => {
    const name = getImportFileName(file)
    return datasetFilePattern.test(name)
  })
}

function handleDatasetFolderChange(event) {
  const files = filterSupportedDatasetFiles(event.target.files)

  if (!files.length) {
    datasetUploadFiles.value = []
    datasetUploadMode.value = ""
    datasetUploadName.value = ""
    proxy.$modal.msgWarning("鎵€閫夋枃浠跺す涓湭璇嗗埆鍒?MAT銆丆SV銆乀XT銆乆LSX 鏁版嵁鏂囦欢")
    return
  }

  datasetUploadFiles.value = files
  datasetUploadMode.value = "folder"
  datasetUploadName.value = getFolderNameFromFiles(files)

  if (datasetSingleFileInputRef.value) {
    datasetSingleFileInputRef.value.value = ""
  }

  proxy.$modal.msgSuccess(`宸查€夋嫨鏂囦欢澶癸細${datasetUploadName.value}锛岃瘑鍒埌 ${files.length} 涓湁鏁堟暟鎹枃浠禶)
}

function handleDatasetSingleFileChange(event) {
  const files = filterSupportedDatasetFiles(event.target.files)

  if (!files.length) {
    datasetUploadFiles.value = []
    datasetUploadMode.value = ""
    datasetUploadName.value = ""
    proxy.$modal.msgWarning("浠呮敮鎸?MAT銆丆SV銆乀XT銆乆LSX 鏁版嵁鏂囦欢")
    return
  }

  const file = files[0]
  datasetUploadFiles.value = [file]
  datasetUploadMode.value = "file"
  datasetUploadName.value = getImportFileName(file)

  if (datasetFolderInputRef.value) {
    datasetFolderInputRef.value.value = ""
  }
}

function handleImportDataset() {
  if (!datasetUploadFiles.value.length) {
    proxy.$modal.msgWarning("璇峰厛閫夋嫨闇€瑕佸鍏ョ殑鏁版嵁闆嗘枃浠跺す鎴栧崟涓暟鎹枃浠?)
    return
  }

  const datasetId = Date.now()
  const datasetName = datasetUploadMode.value === "folder"
      ? datasetUploadName.value
      : datasetUploadName.value.replace(datasetFilePattern, "")

  const newDataset = {
    datasetId,
    datasetName,
    datasetType: datasetUploadMode.value === "folder" ? "鏂囦欢澶规暟鎹泦" : "鍗曟枃浠舵暟鎹泦",
    fileCount: datasetUploadFiles.value.length,
    sampleCount: datasetUploadFiles.value.length * 2
  }

  datasetOptions.value.unshift(newDataset)
  Object.assign(datasetForm, newDataset)
  appendImportedRawFiles(datasetUploadFiles.value, datasetId)

  preprocessStatus.value = "寰呭鐞?
  processedSampleList.value = []
  currentPipelineId.value = null
  canGoAugment.value = false

  proxy.$modal.msgSuccess("鏁版嵁闆嗗鍏ユ垚鍔燂紝鍘熷鏁版嵁鏂囦欢宸插姞鍏ュ垪琛紝璇风户缁厤缃暟鎹澶勭悊鍙傛暟")
}

function appendImportedRawFiles(files, datasetId) {
  const now = Date.now()
  const rows = files.map((file, index) => {
    const fileName = getImportFileName(file)
    const suffixMatch = fileName.match(/\.([^.]+)$/)
    const suffix = suffixMatch ? suffixMatch[1].toLowerCase() : ""
    const relativePath = getImportFileRelativePath(file)

    return {
      fileId: now + index,
      datasetId,
      fileCode: `FILE-IMP-${String(index + 1).padStart(3, "0")}`,
      originalFileName: fileName,
      fileSuffix: suffix,
      fileType: suffix,
      fileSize: file.size || 0,
      storagePath: relativePath ? `/data/import/${relativePath}` : `/data/import/${fileName}`,
      fileMd5: "-",
      sourceType: datasetUploadMode.value === "folder" ? "鏂囦欢澶瑰鍏? : "鏂囦欢涓婁紶",
      importStatus: "鎴愬姛",
      parseStatus: "鎴愬姛",
      sampleCount: 2,
      errorMsg: null,
      delFlag: "0",
      remark: datasetUploadMode.value === "folder" ? `鏉ヨ嚜鏂囦欢澶癸細${datasetUploadName.value}` : "鍗曟枃浠跺鍏?
    }
  })

  fileofdaList.value = [...rows, ...fileofdaList.value]
  total.value = Number(total.value || 0) + rows.length
}

function handleResetDatasetImport() {
  datasetUploadFiles.value = []
  datasetUploadMode.value = ""
  datasetUploadName.value = ""

  if (datasetFolderInputRef.value) {
    datasetFolderInputRef.value.value = ""
  }

  if (datasetSingleFileInputRef.value) {
    datasetSingleFileInputRef.value.value = ""
  }
}

function handleDatasetChange(datasetId) {
  const dataset = datasetOptions.value.find(item => item.datasetId === datasetId)

  if (!dataset) {
    return
  }

  Object.assign(datasetForm, dataset)
  preprocessStatus.value = "寰呭鐞?
  processedSampleList.value = []
  currentPipelineId.value = null
  canGoAugment.value = false
}

function buildPreprocessFlowText() {
  const flow = []

  if (preprocessForm.steps.includes("denoise")) {
    flow.push(getDenoiseMethodName(preprocessForm.denoiseMethod))
  }

  if (preprocessForm.steps.includes("normalize")) {
    flow.push(getNormalizeMethodName(preprocessForm.normalizeMethod))
  }

  flow.push("鏁版嵁鍒嗗抚")

  return flow.join(" 鈫?")
}

function getDenoiseMethodName(value) {
  const map = {
    wavelet: "灏忔尝鍘诲櫔",
    butterworth: "宸寸壒娌冩柉甯﹂€氭护娉?,
    hht: "甯屽皵浼壒榛勫彉鎹?
  }

  return map[value] || "-"
}

function getNormalizeMethodName(value) {
  const map = {
    zscore: "Z-score 鏍囧噯鍖?,
    minmax: "Min-Max 褰掍竴鍖?
  }

  return map[value] || "-"
}

async function handleRunPreprocess() {
  if (!currentDataset.value) {
    proxy.$modal.msgWarning("璇峰厛閫夋嫨鏁版嵁闆?)
    return
  }

  if (!preprocessForm.steps.includes("frame")) {
    preprocessForm.steps.push("frame")
  }

  const selectedFileIds = ids.value && ids.value.length
      ? ids.value
      : fileofdaList.value.map(item => item.fileId).filter(Boolean)

  if (!selectedFileIds.length) {
    proxy.$modal.msgWarning("璇峰厛鍕鹃€夐渶瑕侀澶勭悊鐨勬暟鎹枃浠?)
    return
  }

  preprocessStatus.value = "澶勭悊涓?.."

  const pipeline = createTopic4Pipeline({
    datasetId: datasetForm.datasetId,
    datasetName: datasetForm.datasetName
  })

  const data = {
    pipelineId: pipeline.pipelineId,
    datasetId: datasetForm.datasetId,
    datasetCode: datasetForm.datasetCode || "CWRU",
    fileIds: selectedFileIds,
    windowSize: Number(preprocessForm.windowSize || 1024),
    stride: Number(preprocessForm.stride || 512),
    overlapRate: Number(preprocessForm.overlapRate || 50),
    denoise: preprocessForm.steps.includes("denoise"),
    normalize: preprocessForm.steps.includes("normalize"),
    frame: true,
    denoiseMethod: preprocessForm.denoiseMethod,
    normalizeMethod: preprocessForm.normalizeMethod,
    methodName: buildPreprocessFlowText()
  }

  try {
    const res = await executePreprocess(data)

    if (res.code !== 200) {
      preprocessStatus.value = "澶辫触"
      proxy.$modal.msgError(res.msg || "鏁版嵁棰勫鐞嗗け璐?)
      return
    }

    const samples = await loadProcessedSamples()
    preprocessStatus.value = "宸插畬鎴?

    updateTopic4Pipeline(pipeline.pipelineId, {
      currentStage: "PREPROCESSED",
      processedSamples: samples
    })

    currentPipelineId.value = pipeline.pipelineId
    canGoAugment.value = true

    await getList()
    proxy.$modal.msgSuccess("鏁版嵁棰勫鐞嗗畬鎴愶紝鍙偣鍑昏繘鍏ユ牱鏈寮虹户缁笅涓€姝?)
  } catch (error) {
    console.error("鏁版嵁棰勫鐞嗘帴鍙ｈ皟鐢ㄥけ璐ワ細", error)
    preprocessStatus.value = "澶辫触"
    proxy.$modal.msgError("鏁版嵁棰勫鐞嗘帴鍙ｈ皟鐢ㄥけ璐ワ紝璇锋鏌ュ悗绔?/system/fileofda/preprocess 鏄惁鍙敤")
  }
}

async function loadProcessedSamples() {
  try {
    const res = await listRawSamples()
    const rows = res.rows || res.data || []
    const samples = rows.map(item => ({
      sampleId: item.sampleId,
      sampleCode: item.sampleCode,
      sourceFile: item.sourceFile || item.originalFileName || item.fileName || item.filePath || "-",
      windowSize: item.windowSize || preprocessForm.windowSize,
      stride: item.stride || preprocessForm.stride,
      overlapRate: item.overlapRate || `${preprocessForm.overlapRate}%`,
      denoiseMethod: preprocessForm.steps.includes("denoise") ? getDenoiseMethodName(preprocessForm.denoiseMethod) : "鏈惎鐢?,
      normalizeMethod: preprocessForm.steps.includes("normalize") ? getNormalizeMethodName(preprocessForm.normalizeMethod) : "鏈惎鐢?,
      sampleShape: item.sampleShape || `${item.windowSize || preprocessForm.windowSize}脳1`,
      processStatus: item.preprocessStatus || item.sampleStatus || "宸茬敓鎴?,
      createTime: item.createTime || formatDateTime(new Date()),
      sampleFilePath: item.sampleFilePath || item.samplePath || item.remark || ""
    }))

    processedSampleList.value = samples
    return samples
  } catch (error) {
    console.error("棰勫鐞嗘牱鏈煡璇㈠け璐ワ細", error)
    processedSampleList.value = []
    return []
  }
}

function handleGoAugment() {
  if (!currentPipelineId.value) {
    proxy.$modal.msgWarning("璇峰厛鎵ц鏁版嵁棰勫鐞?)
    return
  }

  goTopic4PageByTitle("鏍锋湰澧炲己", {
    pipelineId: currentPipelineId.value
  })
}

function goTopic4PageByTitle(title, query = {}) {
  const routes = proxy.$router.getRoutes()

  const targetRoute = routes.find(route => {
    return route.meta && route.meta.title === title
  })

  if (!targetRoute) {
    proxy.$modal.msgError(`娌℃湁鎵惧埌鑿滃崟璺敱锛?{title}锛岃妫€鏌ュ乏渚ц彍鍗曞悕绉版槸鍚︿竴鑷碻)
    console.table(
        routes
            .filter(route => route.meta && route.meta.title)
            .map(route => ({
              title: route.meta.title,
              path: route.path,
              name: route.name
            }))
    )
    return
  }

  proxy.$router.push({
    path: targetRoute.path,
    query
  })
}

function buildProcessedSampleRows() {
  const denoiseName = preprocessForm.steps.includes("denoise")
      ? getDenoiseMethodName(preprocessForm.denoiseMethod)
      : "鏈惎鐢?

  const normalizeName = preprocessForm.steps.includes("normalize")
      ? getNormalizeMethodName(preprocessForm.normalizeMethod)
      : "鏈惎鐢?

  const sourceFiles = getPreprocessSourceFiles()

  return sourceFiles.flatMap((file, fileIndex) => {
    const frameCount = Math.max(1, Number(file.sampleCount || 2))

    return Array.from({ length: frameCount }).map((_, frameIndex) => {
      const id = fileIndex * 10 + frameIndex + 1

      return {
        sampleId: id,
        sampleCode: `SAMPLE-${String(id).padStart(3, "0")}`,
        datasetId: datasetForm.datasetId,
        sourceFileName: file.sourceFileName,
        faultType: file.faultType,
        faultLocation: file.faultLocation,
        windowSize: preprocessForm.windowSize,
        stride: preprocessForm.stride,
        overlapRate: preprocessForm.overlapRate,
        denoiseMethod: denoiseName,
        normalizeMethod: normalizeName,
        sampleShape: `${preprocessForm.windowSize}脳1`,
        status: "宸茬敓鎴?,
        createTime: formatDateTime(new Date())
      }
    })
  })
}

function getPreprocessSourceFiles() {
  const rows = (fileofdaList.value || []).filter(item => {
    if (!currentDataset.value) {
      return true
    }

    return Number(item.datasetId || currentDataset.value.datasetId) === Number(currentDataset.value.datasetId)
  })

  if (rows.length > 0) {
    return rows.map(item => {
      const fileName = item.originalFileName || item.fileCode || "unknown.mat"
      const faultInfo = inferFaultInfo(fileName)

      return {
        sourceFileName: fileName,
        sampleCount: Number(item.sampleCount || 2),
        faultType: faultInfo.faultType,
        faultLocation: faultInfo.faultLocation
      }
    })
  }

  return [
    {
      sourceFileName: "normal_0hp.mat",
      faultType: "姝ｅ父",
      faultLocation: "鏃?,
      sampleCount: 2
    },
    {
      sourceFileName: "inner_race_0hp_007.mat",
      faultType: "鍐呭湀鏁呴殰",
      faultLocation: "杞存壙鍐呭湀",
      sampleCount: 2
    },
    {
      sourceFileName: "ball_0hp_007.mat",
      faultType: "婊氬姩浣撴晠闅?,
      faultLocation: "杞存壙婊氬姩浣?,
      sampleCount: 2
    },
    {
      sourceFileName: "outer_race_0hp_007.mat",
      faultType: "澶栧湀鏁呴殰",
      faultLocation: "杞存壙澶栧湀",
      sampleCount: 2
    },
    {
      sourceFileName: "inner_race_1hp_014.mat",
      faultType: "鍐呭湀鏁呴殰",
      faultLocation: "杞存壙鍐呭湀",
      sampleCount: 2
    },
    {
      sourceFileName: "outer_race_2hp_021.mat",
      faultType: "澶栧湀鏁呴殰",
      faultLocation: "杞存壙澶栧湀",
      sampleCount: 2
    }
  ]
}

function inferFaultInfo(fileName) {
  const name = String(fileName || "").toLowerCase()

  if (name.includes("normal")) {
    return {
      faultType: "姝ｅ父",
      faultLocation: "鏃?
    }
  }

  if (name.includes("inner")) {
    return {
      faultType: "鍐呭湀鏁呴殰",
      faultLocation: "杞存壙鍐呭湀"
    }
  }

  if (name.includes("outer")) {
    return {
      faultType: "澶栧湀鏁呴殰",
      faultLocation: "杞存壙澶栧湀"
    }
  }

  if (name.includes("ball")) {
    return {
      faultType: "婊氬姩浣撴晠闅?,
      faultLocation: "杞存壙婊氬姩浣?
    }
  }

  return {
    faultType: "鏈煡",
    faultLocation: "鏈煡"
  }
}

function handleExportProcessedSamples() {
  if (!processedSampleList.value.length) {
    proxy.$modal.msgWarning("鏆傛棤鍙鍑虹殑棰勫鐞嗘牱鏈?)
    return
  }

  proxy.$modal.msgSuccess("棰勫鐞嗘牱鏈垪琛ㄥ凡瀵煎嚭")
}

function formatDateTime(date) {
  const pad = value => String(value).padStart(2, "0")

  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hour = pad(date.getHours())
  const minute = pad(date.getMinutes())
  const second = pad(date.getSeconds())

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.fileId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  form.value.datasetId = 1
  form.value.fileCode = "FILE-CWRU-" + String(new Date().getTime()).slice(-3)
  form.value.fileSuffix = "mat"
  form.value.fileType = "mat"
  form.value.fileSize = 0
  form.value.sourceType = "CWRU"
  form.value.importStatus = "鎴愬姛"
  form.value.parseStatus = "鎴愬姛"
  form.value.sampleCount = 2
  open.value = true
  title.value = "娣诲姞鏁版嵁鏂囦欢"
}

function handleUpdate(row) {
  reset()
  const fileId = row.fileId || ids.value
  getFileofda(fileId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "淇敼鏁版嵁鏂囦欢"
  })
}

function submitForm() {
  proxy.$refs["fileofdaRef"].validate(valid => {
    if (!valid) {
      return
    }

    if (form.value.fileId != null) {
      updateFileofda(form.value).then(() => {
        proxy.$modal.msgSuccess("淇敼鎴愬姛")
        open.value = false
        getList()
      })
    } else {
      addFileofda(form.value).then(() => {
        proxy.$modal.msgSuccess("鏂板鎴愬姛")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const fileIds = row.fileId || ids.value
  proxy.$modal.confirm('鏄惁纭鍒犻櫎鏁版嵁鏂囦欢缂栧彿涓?"' + fileIds + '" 鐨勬暟鎹」锛?).then(() => {
    return delFileofda(fileIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("鍒犻櫎鎴愬姛")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("system/fileofda/export", {
    ...queryParams.value
  }, `fileofda_${new Date().getTime()}.xlsx`)
}

function statusTagType(value) {
  if (value === "鎴愬姛") {
    return "success"
  }

  if (value === "澶辫触") {
    return "danger"
  }

  if (value === "鏈В鏋? || value === "鏈鍏?) {
    return "warning"
  }

  return "info"
}

function formatFileSize(size) {
  const value = Number(size || 0)

  if (value <= 0) {
    return "0 B"
  }

  if (value < 1024) {
    return value + " B"
  }

  if (value < 1024 * 1024) {
    return (value / 1024).toFixed(1) + " KB"
  }

  return (value / 1024 / 1024).toFixed(1) + " MB"
}

getList()
</script>

<style scoped lang="scss">
.project4-page {
  padding: 18px;
  min-height: calc(100vh - 84px);
  background: #eef5fb;
  color: #12213a;
}

.module-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding: 24px 28px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.08);

  h2 {
    margin: 4px 0 8px;
    font-size: 26px;
    font-weight: 800;
    color: #0c2b52;
  }

  p {
    margin: 0;
    color: #4b688c;
    font-size: 14px;
    line-height: 1.8;
  }
}

.module-eyebrow {
  color: #1d7ed0;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.module-status {
  display: flex;
  gap: 10px;
  flex-shrink: 0;

  span {
    padding: 7px 14px;
    border: 1px solid #c9def3;
    border-radius: 999px;
    background: #ffffff;
    color: #2f5f91;
    font-size: 13px;
    font-weight: 600;
  }
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.metric-mini {
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);

  span {
    display: block;
    color: #6b7f99;
    font-size: 13px;
    font-weight: 700;
  }

  strong {
    display: block;
    margin: 10px 0 4px;
    color: #0d1b2f;
    font-size: 28px;
    font-weight: 800;
  }

  em {
    color: #1d7ed0;
    font-size: 12px;
    font-style: normal;
  }
}

.dataset-card,
.preprocess-card,
.table-card {
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);
}

.dataset-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.preprocess-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 14px;
  align-items: stretch;
}

.preprocess-main {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 14px;
}

.frame-param-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dataset-panel,
.preprocess-panel {
  padding: 14px 16px;
  border: 1px solid #d6e7f7;
  border-radius: 14px;
  background: #ffffff;
}

.panel-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 16px;
  font-weight: 800;
}

.section-desc {
  margin: 6px 0 0;
  color: #5d728c;
  font-size: 13px;
  line-height: 1.7;
}

.folder-upload-box {
  padding: 22px 18px;
  border: 1px dashed #b8d5f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #ffffff 100%);
  text-align: center;
}

.folder-upload-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 10px;
  border-radius: 16px;
  background: #e8f4ff;
  color: #2f8be6;
  font-size: 26px;
}

.folder-upload-title {
  margin-bottom: 8px;
  color: #0c2b52;
  font-size: 16px;
  font-weight: 800;
}

.folder-upload-desc {
  max-width: 560px;
  margin: 0 auto 16px;
  color: #6b7f99;
  font-size: 13px;
  line-height: 1.7;
}

.folder-upload-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 14px;
}

.hidden-file-input {
  display: none;
}

.selected-dataset-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  max-width: 620px;
  margin: 0 auto;
  padding: 10px 12px;
  border: 1px solid #d8ebd2;
  border-radius: 12px;
  background: #f5fff2;
  color: #244568;
  text-align: left;

  span {
    display: block;
    margin-bottom: 4px;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    color: #0c2b52;
    font-size: 14px;
  }
}

.upload-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.preprocess-summary {
  display: flex;
  flex-direction: column;
}

.summary-item {
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 1px solid #dbeaf8;
  border-radius: 12px;
  background: #f5f9ff;

  span {
    display: block;
    margin-bottom: 6px;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    color: #0c2b52;
    font-size: 14px;
    font-weight: 800;
    line-height: 1.6;
  }
}

:deep(.el-upload-dragger) {
  border-radius: 14px;
  border-color: #c7dcf2;
  background: #f7fbff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    margin: 4px 0 0;
    font-size: 20px;
    font-weight: 800;
    color: #0c2b52;
  }
}

.table-card-header {
  margin-bottom: 12px;
}

.processed-table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.processed-action-btn {
  width: 132px;
  height: 36px;
  justify-content: center;
}

.raw-query-box {
  margin-bottom: 14px;
  padding: 14px 16px 2px;
  border: 1px solid #d6e7f7;
  border-radius: 14px;
  background: #f8fbff;
}

.inline-section-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 15px;
  font-weight: 800;
}

.raw-query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

.query-actions {
  margin-left: auto;
}

.mb8 {
  margin-bottom: 14px;
}

:deep(.el-form--inline .el-form-item) {
  margin-right: 28px;
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: #244568;
  font-weight: 700;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #d6e4f2 inset;
}

:deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
}

:deep(.el-table) {
  border-radius: 14px;
  overflow: hidden;
  color: #243b57;
}

:deep(.el-table th.el-table__cell) {
  background: #f3f8fd;
  color: #244568;
  font-weight: 800;
}

:deep(.el-table td.el-table__cell) {
  padding: 13px 0;
}

:deep(.el-table .cell) {
  line-height: 1.6;
}

:deep(.el-table__row:hover > td.el-table__cell) {
  background: #f1f8ff !important;
}

:deep(.pagination-container) {
  margin-top: 18px;
  background: transparent;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 10px;
}

:deep(.el-dialog__title) {
  font-weight: 800;
  color: #0c2b52;
}

@media screen and (max-width: 1400px) {
  .metric-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dataset-grid,
  .preprocess-layout,
  .preprocess-main,
  .frame-param-grid {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 1200px) {
  .module-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}

@media screen and (max-width: 768px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }
}
</style>
