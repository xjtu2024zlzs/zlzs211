<template>
  <div class="project4-page">
    <!-- 椤甸潰澶撮儴 -->
    <section class="module-hero">
      <div>
        <div class="module-eyebrow">璇鹃鍥?路 鏍瑰洜鍒嗘瀽缁撴灉杈撳嚭鎺ュ彛</div>
        <h2>鏍瑰洜鍒嗘瀽缁撴灉</h2>
        <p>
          鏈ā鍧楁寜鐓ф帴鍙ｈ〃鏍艰姹傦紝闈㈠悜璇鹃浜旇緭鍑烘牴鍥犲垽鏂€佽瘉鎹摼銆佹牴鍥犲垎鏋愮疆淇″害绛夌粨鏋滐紝
          鐢ㄤ簬鏀拺鐢熷懡鍛ㄦ湡璐ㄩ噺杩芥函銆侀棶棰橀棴鐜€佹暣鏀瑰彫鍥炲拰璐ㄩ噺鍙嶉銆?
        </p>
      </div>

      <div class="module-status">
        <span>璇鹃鍥?鈫?璇鹃浜?/span>
        <span>鏍瑰洜鍒嗘瀽杈撳嚭</span>
      </div>
    </section>

    <!-- 缁熻姒傝 -->
    <section class="confidence-overview stats-only-overview">
      <div class="confidence-left">
        <div class="section-title-row">
          <div>
            <div class="module-eyebrow">缁熻姒傝</div>
            <h3>鏍瑰洜鍒嗘瀽缃俊搴︽瑙?/h3>
          </div>
          <el-tag effect="plain" type="primary">鎸夋渶澶ф牴鍥犵疆淇″害缁熻</el-tag>
        </div>

        <div class="metric-grid">
          <div class="metric-mini">
            <span>鏍锋湰鎬绘暟</span>
            <strong>{{ confidenceStatsData.total }}</strong>
            <em>姣忎釜鏍锋湰灞曠ず鏈€澶х疆淇″害鏍瑰洜</em>
          </div>

          <div class="metric-mini">
            <span>宸插垎鏋愭牱鏈?/span>
            <strong>{{ confidenceStatsData.analyzedCount }}</strong>
            <em>analysisStatus = 宸插垎鏋?/em>
          </div>

          <div class="metric-mini">
            <span>寰呭鏍告牱鏈?/span>
            <strong>{{ confidenceStatsData.pendingCount }}</strong>
            <em>analysisStatus = 寰呭鏍?/em>
          </div>

          <div class="metric-mini">
            <span>骞冲潎鏈€楂樻牴鍥犵疆淇″害</span>
            <strong>{{ formatProbability(confidenceStatsData.avgConfidence) }}</strong>
            <em>avg(max(rootCauseConfidence))</em>
          </div>
        </div>
      </div>
    </section>

    <!-- 璇婃柇缁撴灉杈撳叆涓庢牴鍥犲垎鏋愭墽琛?-->
    <section class="rootcause-input-card">
      <div class="card-header rootcause-input-header">
        <div>
          <div class="module-eyebrow">璇婃柇缁撴灉杈撳叆</div>
          <h3>閫夋嫨璇婃柇鏍锋湰骞舵墽琛屾牴鍥犲垎鏋?/h3>
          <p class="section-desc">
            浠庢晠闅滆瘖鏂ā鍧楁帴鍏ュ凡璇婃柇鏍锋湰锛屽嬀閫夐渶瑕佽拷婧殑寮傚父鏍锋湰鍚庢墽琛屾牴鍥犲垎鏋愶紝缁撴灉灏嗚緭鍑哄埌涓嬫柟鏍瑰洜鍒嗘瀽缁撴灉璁板綍骞跺悓姝ュ埛鏂伴ゼ鍥俱€?
          </p>
        </div>

        <el-tag type="primary" effect="plain">
          宸叉帴鍏ヨ瘖鏂粨鏋滐細{{ diagnosisInputList.length }} 鏉?
        </el-tag>
      </div>

      <div class="rootcause-input-grid">
        <div class="input-table-panel">
          <div class="panel-title-row">
            <div>
              <h4>璇婃柇缁撴灉鏍锋湰鍒楄〃</h4>
              <p>鍕鹃€夐渶瑕佽繘琛屾牴鍥犲垎鏋愮殑璇婃柇鏍锋湰锛屾甯告牱鏈皢鑷姩璺宠繃銆?/p>
            </div>
            <el-tag type="success" effect="plain">宸查€?{{ selectedDiagnosisRows.length }} 鏉?/el-tag>
          </div>

          <el-table
              :data="diagnosisInputList"
              border
              stripe
              height="300"
              empty-text="璇峰厛浠庢晠闅滆瘖鏂ā鍧楄繘鍏ワ紝鎴栧厛鎵ц鏁呴殰璇婃柇"
              @selection-change="handleDiagnosisInputSelection"
              @row-click="handleDiagnosisInputRowClick"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="璇婃柇ID" prop="diagnosisId" align="center" width="90" />
            <el-table-column label="鏍锋湰ID" prop="sampleId" align="center" width="90" />
            <el-table-column label="鏍锋湰缂栧彿" prop="sampleCode" align="center" width="150" show-overflow-tooltip />
            <el-table-column label="鏁呴殰浣嶇疆" prop="faultLocation" align="center" width="130" />
            <el-table-column label="鏁呴殰绫诲瀷" prop="faultType" align="center" width="140">
              <template #default="scope">
                <el-tag :type="diagnosisFaultTag(scope.row.faultType)" effect="plain">
                  {{ scope.row.faultType || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="璇婃柇缃俊搴? align="center" width="130">
              <template #default="scope">
                <span class="confidence-text">{{ formatProbability(scope.row.confidence) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="鍋ュ悍璇勫垎" prop="healthScore" align="center" width="110" />
            <el-table-column label="璇婃柇鏃堕棿" prop="diagnosisTime" align="center" width="170" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="input-action-panel">
          <div class="panel-title-row compact">
            <div>
              <h4>鏍瑰洜鍒嗘瀽鎵ц</h4>
              <p>鍩轰簬璇婃柇缁撴灉銆佽瀺鍚堢壒寰佸拰鏍锋湰閾捐矾鐢熸垚鏍瑰洜璇佹嵁閾俱€?/p>
            </div>
          </div>

          <div class="input-summary-list">
            <div class="summary-box">
              <span>褰撳墠娴佺▼ID</span>
              <strong>{{ pipelineId || '-' }}</strong>
            </div>
            <div class="summary-box">
              <span>宸查€夋嫨鏍锋湰</span>
              <strong>{{ selectedDiagnosisRows.length }}</strong>
            </div>
            <div class="summary-box">
              <span>鍙垎鏋愬紓甯告牱鏈?/span>
              <strong>{{ selectedAbnormalDiagnosisCount }}</strong>
            </div>
            <div class="summary-box">
              <span>杈撳嚭缁撴灉</span>
              <strong>鍏蜂綋鏍瑰洜 + 缃俊搴?+ 璇佹嵁閾?+ 鏁存敼寤鸿</strong>
            </div>
          </div>

          <el-button
              type="primary"
              icon="Operation"
              class="run-rootcause-btn"
              :disabled="selectedAbnormalDiagnosisCount === 0"
              @click="handleRunRootCause"
          >
            鎵ц鏍瑰洜鍒嗘瀽
          </el-button>
        </div>
      </div>

      <div v-if="hasRootCauseResults" class="rootcause-chart-panel">
        <div class="chart-header">
          <div>
            <div class="module-eyebrow">鍥惧舰鍒嗘瀽</div>
            <h3>褰撳墠鏍锋湰鍚勬牴鍥犵疆淇″害楗煎浘</h3>
          </div>

          <div class="sample-chart-actions">
            <el-select
                v-model="selectedChartSampleId"
                placeholder="璇烽€夋嫨鏍锋湰"
                clearable
                style="width: 190px"
                @change="handleChartRecordChange"
            >
              <el-option
                  v-for="item in chartRecordOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>

            <el-button link type="primary" icon="Refresh" @click="refreshChartData">
              鍒锋柊
            </el-button>
          </div>
        </div>

        <div ref="confidenceChartRef" class="confidence-chart rootcause-inline-chart"></div>

        <div class="chart-legend-note">
          <span>鏁版嵁鏉ユ簮锛氬綋鍓嶅嬀閫夎瘖鏂牱鏈敓鎴愮殑鏍瑰洜鍒嗗竷</span>
          <span>楗煎浘灞曠ず鍏ㄩ儴鏍瑰洜锛涜〃鏍煎彧灞曠ず璇ユ牱鏈疆淇″害鏈€澶х殑鏍瑰洜</span>
          <span>鐐瑰嚮璇婃柇杈撳叆琛屾垨缁撴灉琛ㄦ牸琛屽彲鍚屾鍒囨崲鏍锋湰</span>
        </div>

        <div class="evidence-chain-panel">
          <div class="evidence-chain-header">
            <div>
              <div class="module-eyebrow">璇佹嵁閾惧彲瑙嗗寲</div>
              <h4>褰撳墠鏍锋湰鏍瑰洜鎺ㄧ悊璇佹嵁閾?/h4>
              <p>灏嗚瘖鏂緭鍏ャ€佸紓甯哥壒寰併€佸€欓€夋牴鍥犮€佹渶澶ф牴鍥犲拰鏁存敼寤鸿涓茶仈灞曠ず锛屼綋鐜版牴鍥犲垽鏂殑鎺ㄧ悊渚濇嵁銆?/p>
            </div>
            <el-tag type="primary" effect="plain">鏍锋湰锛歿{ currentEvidenceView.sampleCode }}</el-tag>
          </div>

          <div class="evidence-flow-grid">
            <template v-for="(step, index) in currentEvidenceView.steps" :key="step.title">
              <div class="evidence-flow-card" :class="step.type">
                <div class="evidence-step-index">{{ index + 1 }}</div>
                <div class="evidence-step-main">
                  <span>{{ step.title }}</span>
                  <strong>{{ step.primary }}</strong>
                  <p>{{ step.desc }}</p>
                </div>
              </div>
              <div v-if="index < currentEvidenceView.steps.length - 1" class="evidence-arrow">鈫?/div>
            </template>
          </div>

          <div class="fault-formation-chain">
            <div class="formation-title">鏁呴殰褰㈡垚鏈虹悊閾捐矾</div>
            <div class="formation-steps">
              <template v-for="(item, index) in currentEvidenceView.formation" :key="index">
                <span class="formation-pill">{{ item.title }}</span>
                <span v-if="index < currentEvidenceView.formation.length - 1" class="formation-arrow">鈫?/span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 琛ㄦ牸鍖哄煙 -->
    <section v-if="hasRootCauseResults" class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">鏁版嵁鍒楄〃</div>
          <h3>鏍瑰洜鍒嗘瀽缁撴灉璁板綍</h3>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <div v-show="showSearch" class="table-filter-panel">
        <div class="table-filter-title">鏍瑰洜鍒嗘瀽缁撴灉鏌ヨ</div>
        <el-form
            :model="queryParams"
            ref="queryRef"
            :inline="true"
            label-width="96px"
            class="table-filter-form"
        >
          <el-form-item label="鏍瑰洜缂栧彿" prop="analysisCode">
            <el-input
                v-model="queryParams.analysisCode"
                placeholder="璇疯緭鍏ユ牴鍥犵紪鍙?
                clearable
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="鏍锋湰ID" prop="sampleId">
            <el-input-number
                v-model="queryParams.sampleId"
                :controls="false"
                :min="0"
                placeholder="璇疯緭鍏ユ牱鏈琁D"
                style="width: 170px"
            />
          </el-form-item>

          <el-form-item label="璇婃柇ID" prop="diagnosisId">
            <el-input-number
                v-model="queryParams.diagnosisId"
                :controls="false"
                :min="0"
                placeholder="璇疯緭鍏ヨ瘖鏂璉D"
                style="width: 170px"
            />
          </el-form-item>

          <el-form-item label="鍏蜂綋鏍瑰洜" prop="rootCauseType">
            <el-select
                v-model="queryParams.rootCauseType"
                placeholder="璇烽€夋嫨鍏蜂綋鏍瑰洜"
                clearable
                style="width: 210px"
            >
              <el-option
                  v-for="item in rootCauseTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="鍒嗘瀽鐘舵€? prop="analysisStatus">
            <el-select
                v-model="queryParams.analysisStatus"
                placeholder="璇烽€夋嫨鐘舵€?
                clearable
                style="width: 170px"
            >
              <el-option label="宸插垎鏋? value="宸插垎鏋? />
              <el-option label="寰呭鏍? value="寰呭鏍? />
              <el-option label="寰呭垎鏋? value="寰呭垎鏋? />
            </el-select>
          </el-form-item>

          <el-form-item label="鍒嗘瀽浜? prop="analyst">
            <el-input
                v-model="queryParams.analyst"
                placeholder="璇疯緭鍏ュ垎鏋愪汉"
                clearable
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="鍒嗘瀽鏃堕棿" prop="analysisTime">
            <el-date-picker
                v-model="queryParams.analysisTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="璇烽€夋嫨鍒嗘瀽鏃堕棿"
                style="width: 170px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">鎼滅储</el-button>
            <el-button icon="Refresh" @click="resetQuery">閲嶇疆</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['system:resultofrc:add']"
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
              v-hasPermi="['system:resultofrc:edit']"
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
              v-hasPermi="['system:resultofrc:remove']"
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
              v-hasPermi="['system:resultofrc:export']"
          >
            瀵煎嚭
          </el-button>
        </el-col>

      </el-row>

      <el-table
          v-loading="loading"
          :data="resultofrcList"
          border
          stripe
          @selection-change="handleSelectionChange"
          @row-click="handleTableRowClick"
          :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="鏍瑰洜ID" align="center" prop="analysisId" width="90" />
        <el-table-column label="鏍瑰洜缂栧彿" align="center" prop="analysisCode" width="150" show-overflow-tooltip />
        <el-table-column label="璇婃柇ID" align="center" prop="diagnosisId" width="90" />
        <el-table-column label="鏍锋湰ID" align="center" prop="sampleId" width="90" />

        <el-table-column label="鏈€澶х疆淇″害鏍瑰洜" align="left" width="390" show-overflow-tooltip>
          <template #default="scope">
            <div class="judgment-cell">
              <el-tag :type="rootCauseTypeTag(scope.row.rootCauseType)" effect="plain">
                {{ scope.row.rootCauseType || "-" }}
              </el-tag>
              <div class="judgment-desc">
                {{ scope.row.rootCauseDesc || "-" }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="璇佹嵁閾炬憳瑕? align="left" width="390" show-overflow-tooltip>
          <template #default="scope">
            <div class="evidence-summary">
              {{ buildEvidenceSummary(scope.row.evidenceJson) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="鏈€澶ф牴鍥犵疆淇″害" align="center" prop="probability" width="150">
          <template #default="scope">
            <span class="confidence-text">
              {{ formatProbability(scope.row.probability) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="鏁存敼寤鸿" align="left" prop="maintenanceSuggestion" width="320" show-overflow-tooltip />
        <el-table-column label="鍒嗘瀽鏂规硶" align="center" prop="analysisMethod" width="220" show-overflow-tooltip />

        <el-table-column label="鍒嗘瀽鐘舵€? align="center" prop="analysisStatus" width="110">
          <template #default="scope">
            <el-tag :type="analysisStatusTag(scope.row.analysisStatus)" effect="plain">
              {{ scope.row.analysisStatus || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="鍒嗘瀽浜? align="center" prop="analyst" width="150" show-overflow-tooltip />
        <el-table-column label="鍒嗘瀽鏃堕棿" align="center" prop="analysisTime" width="170" />
        <el-table-column label="澶囨敞" align="center" prop="remark" width="260" show-overflow-tooltip />

        <el-table-column label="鎿嶄綔" align="center" width="230" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click.stop="handleDetail(scope.row)">
              璇︽儏
            </el-button>

            <el-button
                link
                type="primary"
                icon="Edit"
                @click.stop="handleUpdate(scope.row)"
                v-hasPermi="['system:resultofrc:edit']"
            >
              淇敼
            </el-button>

            <el-button
                link
                type="primary"
                icon="Delete"
                @click.stop="handleDelete(scope.row)"
                v-hasPermi="['system:resultofrc:remove']"
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

    <!-- 鎺ュ彛瀛楁璇存槑锛氶粯璁ゆ姌鍙狅紝閬垮厤鍗犵敤灞曠ず绌洪棿 -->
    <section v-if="hasRootCauseResults" class="interface-card interface-collapse-card">
      <el-collapse>
        <el-collapse-item title="鎺ュ彛瀛楁璇存槑" name="interfaceFields">
          <div class="interface-grid">
            <div class="interface-item">
              <span>杈撳嚭鏂瑰悜</span>
              <strong>璇鹃鍥?鈫?璇鹃浜?/strong>
            </div>
            <div class="interface-item">
              <span>楗煎浘灞曠ず</span>
              <strong>鏍锋湰涓嬪叏閮ㄥ叿浣撴牴鍥犵疆淇″害</strong>
            </div>
            <div class="interface-item">
              <span>琛ㄦ牸灞曠ず</span>
              <strong>姣忎釜鏍锋湰鏈€澶х疆淇″害鏍瑰洜</strong>
            </div>
            <div class="interface-item">
              <span>涓€鑷存€ц鍒?/span>
              <strong>琛ㄦ牸鏍瑰洜 = 楗煎浘鏈€澶у崰姣旀牴鍥?/strong>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </section>

    <!-- 鏂板 / 淇敼寮圭獥 -->
    <el-dialog :title="title" v-model="open" width="920px" append-to-body>
      <el-form ref="resultofrcRef" :model="form" :rules="rules" label-width="140px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="鏍瑰洜缂栧彿" prop="analysisCode">
              <el-input v-model="form.analysisCode" placeholder="璇疯緭鍏ユ牴鍥犵紪鍙? />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="璇婃柇ID" prop="diagnosisId">
              <el-input-number
                  v-model="form.diagnosisId"
                  :controls="false"
                  :min="0"
                  placeholder="璇疯緭鍏ヨ瘖鏂璉D"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏍锋湰ID" prop="sampleId">
              <el-input-number
                  v-model="form.sampleId"
                  :controls="false"
                  :min="0"
                  placeholder="璇疯緭鍏ユ牱鏈琁D"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鍏蜂綋鏍瑰洜" prop="rootCauseType">
              <el-select v-model="form.rootCauseType" placeholder="璇烽€夋嫨鍏蜂綋鏍瑰洜" style="width: 100%">
                <el-option
                    v-for="item in rootCauseTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鏍瑰洜缃俊搴? prop="probability">
              <el-input-number
                  v-model="form.probability"
                  :controls="false"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  placeholder="璇疯緭鍏?-1涔嬮棿鐨勭疆淇″害"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鍒嗘瀽鐘舵€? prop="analysisStatus">
              <el-select v-model="form.analysisStatus" placeholder="璇烽€夋嫨鍒嗘瀽鐘舵€? style="width: 100%">
                <el-option label="宸插垎鏋? value="宸插垎鏋? />
                <el-option label="寰呭鏍? value="寰呭鏍? />
                <el-option label="寰呭垎鏋? value="寰呭垎鏋? />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鍒嗘瀽鏂规硶" prop="analysisMethod">
              <el-input v-model="form.analysisMethod" placeholder="璇疯緭鍏ュ垎鏋愭柟娉? />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鍒嗘瀽浜? prop="analyst">
              <el-input v-model="form.analyst" placeholder="璇疯緭鍏ュ垎鏋愪汉" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="鍒嗘瀽鏃堕棿" prop="analysisTime">
              <el-date-picker
                  v-model="form.analysisTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="璇烽€夋嫨鍒嗘瀽鏃堕棿"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="鏍瑰洜鍒ゆ柇鎻忚堪" prop="rootCauseDesc">
              <el-input
                  v-model="form.rootCauseDesc"
                  type="textarea"
                  :rows="4"
                  placeholder="璇疯緭鍏ユ牴鍥犲垽鏂弿杩?
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="璇佹嵁閾綣SON" prop="evidenceJson">
              <el-input
                  v-model="form.evidenceJson"
                  type="textarea"
                  :rows="8"
                  placeholder="璇疯緭鍏ヨ瘉鎹摼JSON"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="鏁存敼寤鸿" prop="maintenanceSuggestion">
              <el-input
                  v-model="form.maintenanceSuggestion"
                  type="textarea"
                  :rows="4"
                  placeholder="璇疯緭鍏ユ暣鏀瑰缓璁?
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="澶囨敞" prop="remark">
              <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="璇疯緭鍏ュ娉?
              />
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

    <!-- 璇︽儏寮圭獥 -->
    <el-dialog title="鏍瑰洜鍒嗘瀽缁撴灉璇︽儏" v-model="detailOpen" width="980px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="杈撳嚭鏂瑰悜">璇鹃鍥?鈫?璇鹃浜?/el-descriptions-item>
        <el-descriptions-item label="鎺ュ彛绫诲瀷">鏍瑰洜鍒嗘瀽缁撴灉杈撳嚭鎺ュ彛</el-descriptions-item>
        <el-descriptions-item label="鏍瑰洜ID">{{ detail.analysisId }}</el-descriptions-item>
        <el-descriptions-item label="鏍瑰洜缂栧彿">{{ detail.analysisCode }}</el-descriptions-item>
        <el-descriptions-item label="璇婃柇ID">{{ detail.diagnosisId }}</el-descriptions-item>
        <el-descriptions-item label="鏍锋湰ID">{{ detail.sampleId }}</el-descriptions-item>
        <el-descriptions-item label="鏈€澶ф牴鍥犵疆淇″害">{{ formatProbability(detail.probability) }}</el-descriptions-item>
        <el-descriptions-item label="鍒嗘瀽鐘舵€?>{{ detail.analysisStatus }}</el-descriptions-item>
        <el-descriptions-item label="鍒嗘瀽鏂规硶">{{ detail.analysisMethod }}</el-descriptions-item>
        <el-descriptions-item label="鍒嗘瀽鏃堕棿">{{ detail.analysisTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">褰撳墠鏍锋湰鍏ㄩ儴鏍瑰洜缃俊搴?/el-divider>
      <el-table :data="detail.distribution || []" border size="small">
        <el-table-column label="鍏蜂綋鏍瑰洜" prop="rootCauseType" />
        <el-table-column label="鏍瑰洜绫诲埆" prop="rootCauseCategory" width="130" />
        <el-table-column label="缃俊搴? width="120">
          <template #default="scope">
            {{ Number(scope.row.confidence || 0).toFixed(1) }}%
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">鏈€澶х疆淇″害鏍瑰洜鍒ゆ柇</el-divider>
      <el-input v-model="detail.rootCauseJudgment" type="textarea" :rows="4" readonly />

      <el-divider content-position="left">璇佹嵁閾?/el-divider>
      <el-input v-model="detail.evidenceJson" type="textarea" :rows="14" readonly />

      <el-divider content-position="left">鏁存敼寤鸿</el-divider>
      <el-input v-model="detail.maintenanceSuggestion" type="textarea" :rows="4" readonly />

      <el-divider content-position="left">澶囨敞</el-divider>
      <el-input v-model="detail.remark" type="textarea" :rows="3" readonly />

      <template #footer>
        <el-button type="primary" @click="detailOpen = false">鍏抽棴</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Resultofrc">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs } from "vue"
import * as echarts from "echarts"
import {
  listResultofrc,
  delResultofrc,
  addResultofrc,
  updateResultofrc
} from "@/api/project4/resultofrc"
import { useRoute } from "vue-router"
import {
  getCurrentTopic4PipelineId,
  getTopic4Pipeline,
  updateTopic4Pipeline,
} from "@/utils/project4/topic4Pipeline"

const { proxy } = getCurrentInstance()
const route = useRoute()
const pipelineId = ref(route.query.pipelineId || getCurrentTopic4PipelineId())
const upstreamDiagnosisResults = ref([])
const diagnosisInputList = ref([])
const selectedDiagnosisRows = ref([])
const hasRootCauseResults = ref(false)
const resultofrcList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const detail = ref({})

const confidenceChartRef = ref(null)
let confidenceChartInstance = null

const chartRecordOptions = ref([])
const selectedChartSampleId = ref(null)

const selectedAbnormalDiagnosisCount = computed(() => {
  return selectedDiagnosisRows.value.filter(row => row.faultType !== "姝ｅ父").length
})

const currentEvidenceRow = computed(() => {
  if (!resultofrcList.value || resultofrcList.value.length === 0) {
    return null
  }

  const selected = resultofrcList.value.find(row => String(row.sampleId) === String(selectedChartSampleId.value))
  return selected || resultofrcList.value[0]
})

const currentEvidenceView = computed(() => buildEvidenceView(currentEvidenceRow.value))

/**
 * 鍏釜鍥哄畾鍏蜂綋鏍瑰洜
 * 楗煎浘姘歌繙灞曠ず杩欏叚涓牴鍥狅紱
 * 琛ㄦ牸鍙睍绀烘瘡涓牱鏈腑缃俊搴︽渶澶х殑涓€涓牴鍥犮€?
 */
const ROOT_CAUSE_LIBRARY = [
  {
    key: "preloadHigh",
    rootCauseType: "杞存壙棰勭揣鍔涜缃亸澶?,
    rootCauseCategory: "瑁呴厤鍙傛暟",
    rootCauseDesc: "鍏蜂綋鏍瑰洜锛氳酱鎵块绱у姏璁剧疆鍋忓ぇ锛屽鑷磋繍琛屾俯鍗囧崌楂樸€佹帴瑙﹀簲鍔涘澶э紝骞惰鍙戝紓甯告尟鍔ㄣ€?,
    maintenanceSuggestion: "寤鸿澶嶆牳杞存壙棰勭揣鍙傛暟銆佸畨瑁呭伐鑹鸿褰曞拰杩愯娓╁崌鏁版嵁锛屽棰勭揣鍔涘亸澶х殑瑁呴厤浠惰繘琛岄噸鏂拌皟鏁淬€?
  },
  {
    key: "racewayMicroDamage",
    rootCauseType: "婊氶亾寰皬鎹熶激",
    rootCauseCategory: "琛ㄩ潰鎹熶激",
    rootCauseDesc: "鍏蜂綋鏍瑰洜锛氳酱鎵挎粴閬撳瓨鍦ㄥ井灏忓墺钀姐€佸垝浼ゆ垨鐐硅殌锛岃繍琛屼腑褰㈡垚鍛ㄦ湡鎬у啿鍑荤壒寰併€?,
    maintenanceSuggestion: "寤鸿瀵硅酱鎵挎粴閬撹〃闈㈣繘琛屾樉寰鏌ワ紝閲嶇偣鎺掓煡寰皬瑁傜汗銆佺偣铓€銆佸垝浼ゅ拰灞€閮ㄥ墺钀姐€?
  },
  {
    key: "hardnessFluctuation",
    rootCauseType: "鎵规鏉愭枡纭害娉㈠姩",
    rootCauseCategory: "鏉愭枡涓€鑷存€?,
    rootCauseDesc: "鍏蜂綋鏍瑰洜锛氬悓鎵规鏉愭枡纭害瀛樺湪娉㈠姩锛屽鑷村眬閮ㄦ帴瑙︾柌鍔冲鍛介檷浣庯紝骞舵彁鍓嶅嚭鐜板紓甯告尟鍔ㄧ壒寰併€?,
    maintenanceSuggestion: "寤鸿澶嶆牳鍚屾壒娆℃潗鏂欑‖搴︺€佺儹澶勭悊璁板綍鍜屽叆鍘傛楠屾暟鎹紝瀵瑰紓甯告壒娆¤繘琛岄噸鐐硅拷婧€?
  },
  {
    key: "greaseInsufficient",
    rootCauseType: "娑︽粦鑴傚～鍏呬笉瓒?,
    rootCauseCategory: "娑︽粦缁存姢",
    rootCauseDesc: "鍏蜂綋鏍瑰洜锛氭鼎婊戣剛濉厖閲忎笉瓒虫垨娑︽粦淇濇寔鑳藉姏涓嬮檷锛屽鑷存粴鍔ㄦ帴瑙﹀尯鍩熸懇鎿﹀崌楂樺苟璇卞彂寮傚父鎸姩銆?,
    maintenanceSuggestion: "寤鸿妫€鏌ユ鼎婊戣剛鍨嬪彿銆佸～鍏呴噺鍜岃ˉ鑴傚懆鏈燂紝蹇呰鏃跺鍚屽伐鍐佃澶囨墽琛岃ˉ鑴傚拰娑︽粦鐘舵€佸鏍搞€?
  },
  {
    key: "coaxialityDeviation",
    rootCauseType: "瑁呴厤鍚岃酱搴﹀亸宸?,
    rootCauseCategory: "瑁呴厤宸ヨ壓",
    rootCauseDesc: "鍏蜂綋鏍瑰洜锛氳酱鎵胯閰嶈繃绋嬩腑鍚岃酱搴︽帶鍒朵笉瓒筹紝瀵艰嚧杩愯浆鏃朵骇鐢熷懆鏈熸€у啿鍑诲拰鍋忚浇鎸姩銆?,
    maintenanceSuggestion: "寤鸿澶嶆牳杞存壙搴с€佽浆杞村拰绔洊瑁呴厤鍚岃酱搴︼紝瀵硅秴宸閰嶄欢杩涜閲嶆柊瀹氫綅鍜屾牎鍑嗐€?
  },
  {
    key: "sealFailurePollution",
    rootCauseType: "瀵嗗皝澶辨晥瀵艰嚧姹℃煋鐗╄繘鍏?,
    rootCauseCategory: "鐜姹℃煋",
    rootCauseDesc: "鍏蜂綋鏍瑰洜锛氬瘑灏佺粨鏋勫け鏁堝悗姹℃煋鐗╄繘鍏ヨ酱鎵垮唴閮紝閫犳垚娑︽粦鍔ｅ寲鍜屾帴瑙﹂潰寮傚父纾ㄦ崯銆?,
    maintenanceSuggestion: "寤鸿妫€鏌ュ瘑灏佷欢瀹屾暣鎬с€佹薄鏌撶墿鏉ユ簮鍜屾鼎婊戞薄鏌撶▼搴︼紝瀵瑰瘑灏佸け鏁堟壒娆″紑灞曡拷婧€?
  }
]

/** 鍏被鏍瑰洜鍒拌酱鎵挎晠闅滃舰鎴愮殑鏈虹悊鎺ㄥ閾?*/
function buildFaultFormationProcess(rootCauseInfo, sampleId) {
  const rootCauseType = rootCauseInfo?.rootCauseType || ""

  const processMap = {
    "杞存壙棰勭揣鍔涜缃亸澶?: [
      "杞存壙棰勭揣鍔涜缃亸澶?,
      "婊氬姩浣撲笌婊氶亾鎺ヨЕ杞借嵎鍗囬珮",
      "灞€閮ㄦ帴瑙﹀簲鍔涗笌鎽╂摝鐑鍔?,
      "娑︽粦鑶滃彉钖勫苟鍑虹幇杈圭晫娑︽粦",
      "婊氶亾琛ㄩ潰纾ㄦ崯銆佺偣铓€閫愭鎵╁睍",
      "鍐插嚮鎸姩鍜屾俯鍗囨寔缁寮?,
      "杞存壙鏁呴殰褰㈡垚"
    ],

    "婊氶亾寰皬鎹熶激": [
      "婊氶亾瀛樺湪寰皬鍒掍激銆佺偣铓€鎴栧墺钀?,
      "婊氬姩浣撶粡杩囨崯浼ゅ尯鍩熸椂浜х敓鍛ㄦ湡鎬у啿鍑?,
      "鍐插嚮杞借嵎浣挎崯浼よ竟缂樼户缁墿灞?,
      "灞€閮ㄥ墺钀介潰绉澶у苟寮曡捣鎸姩骞呭€煎崌楂?,
      "寮傚父鍐插嚮鐗瑰緛琚瘖鏂ā鍨嬫崟鑾?,
      "杞存壙婊氶亾鏁呴殰褰㈡垚"
    ],

    "鎵规鏉愭枡纭害娉㈠姩": [
      "鍚屾壒娆℃潗鏂欑‖搴﹀瓨鍦ㄦ尝鍔?,
      "灞€閮ㄥ尯鍩熸姉鐤插姵鑳藉姏涓嬮檷",
      "寰幆杞借嵎浣滅敤涓嬬巼鍏堜骇鐢熷井瑁傜汗",
      "寰绾瑰悜婊氶亾琛ㄩ潰鎵╁睍骞跺舰鎴愮偣铓€",
      "鐐硅殌璇卞彂鍐插嚮鎸姩鍜屽櫔澹板崌楂?,
      "鏉愭枡鐤插姵鍨嬭酱鎵挎晠闅滃舰鎴?
    ],

    "娑︽粦鑴傚～鍏呬笉瓒?: [
      "娑︽粦鑴傚～鍏呬笉瓒虫垨淇濇寔鑳藉姏涓嬮檷",
      "婊氬姩鎺ヨЕ鍖哄煙娌硅啘鍘氬害涓嶈冻",
      "鎽╂摝绯绘暟鍗囬珮骞跺鑷村眬閮ㄦ俯鍗?,
      "婊氶亾涓庢粴鍔ㄤ綋琛ㄩ潰鍑虹幇纾ㄦ崯鍜屾摝浼?,
      "纾ㄦ崯棰楃矑杩涗竴姝ュ姞鍓ф鼎婊戝姡鍖?,
      "娑︽粦澶辨晥鍨嬭酱鎵挎晠闅滃舰鎴?
    ],

    "瑁呴厤鍚岃酱搴﹀亸宸?: [
      "杞存壙瑁呴厤鍚岃酱搴﹀亸宸?,
      "杞酱涓庤酱鎵垮骇鍙楀姏涓績涓嶄竴鑷?,
      "杞存壙闀挎湡鎵垮彈鍋忚浇鍜岄檮鍔犺浇鑽?,
      "婊氶亾灞€閮ㄦ帴瑙﹀簲鍔涢泦涓?,
      "杩愯涓嚭鐜板懆鏈熸€ф尟鍔ㄥ拰灞€閮ㄧ柌鍔虫崯浼?,
      "瑁呴厤鍋忓樊璇卞彂杞存壙鏁呴殰褰㈡垚"
    ],

    "瀵嗗皝澶辨晥瀵艰嚧姹℃煋鐗╄繘鍏?: [
      "瀵嗗皝缁撴瀯澶辨晥瀵艰嚧姹℃煋鐗╄繘鍏ヨ酱鎵垮唴閮?,
      "姹℃煋棰楃矑鐮村潖娑︽粦鑴傛竻娲佸害",
      "婊氬姩鎺ヨЕ闈骇鐢熺（绮掔（鎹?,
      "娑︽粦鎬ц兘涓嬮檷骞惰鍙戝眬閮ㄦ摝浼?,
      "纾ㄦ崯棰楃矑涓庢薄鏌撶墿褰㈡垚鎭舵€у惊鐜?,
      "姹℃煋纾ㄦ崯鍨嬭酱鎵挎晠闅滃舰鎴?
    ]
  }

  const steps = processMap[rootCauseType] || [
    rootCauseType || "鏈煡鏍瑰洜",
    "鏍瑰洜瀵艰嚧灞€閮ㄨ繍琛岀姸鎬佸紓甯?,
    "寮傚父鐘舵€佹寔缁綔鐢ㄤ簬杞存壙鍏抽敭鎺ヨЕ鍖哄煙",
    "灞€閮ㄦ崯浼ら€愭绱Н骞舵斁澶ф尟鍔ㄥ搷搴?,
    "璇婃柇妯″瀷璇嗗埆鍒板紓甯哥壒寰?,
    "杞存壙鏁呴殰褰㈡垚"
  ]

  return steps.map((text, index) => ({
    step: index + 1,
    title: text,
    description: `鏍锋湰${sampleId}锛?{text}`
  }))
}

/**
 * 姣忎釜鏍锋湰鐨勫叚绫诲叿浣撴牴鍥犵疆淇″害鍒嗗竷锛屽崟浣嶄负鐧惧垎姣斻€?
 * 杩欑粍鏁版嵁鐢ㄤ簬浠婂ぉ灞曠ず锛?
 * - 楗煎浘灞曠ず褰撳墠鏍锋湰鐨勫叏閮ㄥ叚涓牴鍥狅紱
 * - 琛ㄦ牸鍙睍绀哄綋鍓嶆牱鏈渶澶х疆淇″害鏍瑰洜锛?
 * - 琛ㄦ牸涓殑鏈€澶ф牴鍥犲繀椤荤瓑浜庨ゼ鍥句腑鏈€澶ф墖鍖恒€?
 */
const SAMPLE_ROOT_CAUSE_DISTRIBUTIONS = {
  1: {
    preloadHigh: 80,
    racewayMicroDamage: 10,
    hardnessFluctuation: 4,
    greaseInsufficient: 3,
    coaxialityDeviation: 2,
    sealFailurePollution: 1
  },
  2: {
    preloadHigh: 8,
    racewayMicroDamage: 12,
    hardnessFluctuation: 6,
    greaseInsufficient: 58,
    coaxialityDeviation: 10,
    sealFailurePollution: 6
  },
  3: {
    preloadHigh: 6,
    racewayMicroDamage: 54,
    hardnessFluctuation: 21,
    greaseInsufficient: 4,
    coaxialityDeviation: 3,
    sealFailurePollution: 12
  },
  4: {
    preloadHigh: 15,
    racewayMicroDamage: 9,
    hardnessFluctuation: 5,
    greaseInsufficient: 7,
    coaxialityDeviation: 56,
    sealFailurePollution: 8
  },
  5: {
    preloadHigh: 5,
    racewayMicroDamage: 10,
    hardnessFluctuation: 18,
    greaseInsufficient: 9,
    coaxialityDeviation: 6,
    sealFailurePollution: 52
  }
}

const rootCauseTypeOptions = ROOT_CAUSE_LIBRARY.map(item => ({
  label: item.rootCauseType,
  value: item.rootCauseType
}))

const confidenceStatsData = reactive({
  total: 0,
  analyzedCount: 0,
  pendingCount: 0,
  avgConfidence: 0,
  highCount: 0,
  mediumCount: 0,
  lowCount: 0
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    analysisCode: null,
    diagnosisId: null,
    sampleId: null,
    rootCauseType: null,
    rootCauseDesc: null,
    analysisMethod: null,
    analysisStatus: null,
    analyst: null,
    analysisTime: null
  },
  rules: {
    analysisCode: [
      { required: true, message: "鏍瑰洜缂栧彿涓嶈兘涓虹┖", trigger: "blur" }
    ],
    diagnosisId: [
      { required: true, message: "璇婃柇ID涓嶈兘涓虹┖", trigger: "blur" }
    ],
    sampleId: [
      { required: true, message: "鏍锋湰ID涓嶈兘涓虹┖", trigger: "blur" }
    ],
    rootCauseType: [
      { required: true, message: "鍏蜂綋鏍瑰洜涓嶈兘涓虹┖", trigger: "change" }
    ],
    rootCauseDesc: [
      { required: true, message: "鏍瑰洜鍒ゆ柇鎻忚堪涓嶈兘涓虹┖", trigger: "blur" }
    ],
    probability: [
      { required: true, message: "鏍瑰洜缃俊搴︿笉鑳戒负绌?, trigger: "blur" }
    ],
    analysisStatus: [
      { required: true, message: "鍒嗘瀽鐘舵€佷笉鑳戒负绌?, trigger: "change" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 鏌ヨ鍒楄〃 */
function getList() {
  loading.value = true

  const pipeline = getTopic4Pipeline(pipelineId.value)

  if (pipeline && pipeline.diagnosisResults && pipeline.diagnosisResults.length > 0) {
    const pipelineRows = pipeline.rootCauseResults || []
    resultofrcList.value = filterSummaryRows(pipelineRows)
    total.value = resultofrcList.value.length
    hasRootCauseResults.value = pipelineRows.length > 0

    buildStatsFromCurrentList()
    buildChartRecordOptions()

    nextTick(() => {
      initConfidenceChart()
    })

    loading.value = false
    return
  }

  listResultofrc(queryParams.value).then(response => {
    const sourceRows = response.rows || []

    /**
     * 浠婂ぉ灞曠ず閫昏緫锛?
     * 涓嶅啀鐩存帴鎶婂悗绔瘡鏉℃牴鍥犻兘閾哄湪琛ㄦ牸閲屻€?
     * 鑰屾槸鎸夋牱鏈仛鍚堬紝姣忎釜鏍锋湰鍙繚鐣欐渶澶х疆淇″害鏍瑰洜锛?
     * 楗煎浘缁х画灞曠ず璇ユ牱鏈叏閮ㄥ叚涓牴鍥犵疆淇″害銆?
     */
    const summaryRows = buildSampleSummaryRows(sourceRows)
    resultofrcList.value = filterSummaryRows(summaryRows)
    total.value = resultofrcList.value.length

    buildStatsFromCurrentList()
    buildChartRecordOptions()

    nextTick(() => {
      initConfidenceChart()
    })

    loading.value = false
  }).catch(error => {
    console.error("鏍瑰洜鍒嗘瀽缁撴灉鏌ヨ澶辫触锛屼娇鐢ㄥ墠绔紨绀烘暟鎹細", error)

    const summaryRows = buildSampleSummaryRows([])
    resultofrcList.value = filterSummaryRows(summaryRows)
    total.value = resultofrcList.value.length

    buildStatsFromCurrentList()
    buildChartRecordOptions()

    nextTick(() => {
      initConfidenceChart()
    })

    loading.value = false
  })
}

function loadPipelineInput() {
  const pipeline = getTopic4Pipeline(pipelineId.value)

  if (!pipeline) {
    upstreamDiagnosisResults.value = []
    diagnosisInputList.value = []
    hasRootCauseResults.value = false
    return
  }

  upstreamDiagnosisResults.value = pipeline.diagnosisResults || []
  hasRootCauseResults.value = !!(pipeline.rootCauseResults && pipeline.rootCauseResults.length > 0)
  diagnosisInputList.value = upstreamDiagnosisResults.value.map((row, index) => ({
    diagnosisId: row.diagnosisId || index + 1,
    diagnosisCode: row.diagnosisCode || `DG-${String(index + 1).padStart(3, "0")}`,
    pipelineId: pipelineId.value,
    datasetId: row.datasetId || pipeline.datasetId || 1,
    sampleId: row.sampleId || index + 1,
    sampleCode: row.sampleCode || `SAMPLE-${String(index + 1).padStart(3, "0")}`,
    fusionId: row.fusionId,
    faultLocation: row.faultLocation || "杞存壙缁勪欢",
    faultType: row.faultType || "杞存壙鏁呴殰",
    confidence: row.confidence ?? row.probability ?? 0.85,
    healthScore: row.healthScore ?? 70,
    diagnosisTime: row.diagnosisTime || row.createTime || "-"
  }))

  selectedDiagnosisRows.value = []

  if (diagnosisInputList.value.length > 0) {
    proxy.$modal.msgSuccess(`宸叉帴鍏ヤ笂涓€姝ヨ瘖鏂粨鏋?${diagnosisInputList.value.length} 鏉)
  }
}

function handleDiagnosisInputSelection(selection) {
  selectedDiagnosisRows.value = selection || []
}

function handleDiagnosisInputRowClick(row) {
  selectedChartSampleId.value = String(row.sampleId)

  nextTick(() => {
    initConfidenceChart()
  })
}

function diagnosisFaultTag(value) {
  if (value === "姝ｅ父") {
    return "success"
  }

  if (String(value || "").includes("鍐呭湀")) {
    return "warning"
  }

  if (String(value || "").includes("澶栧湀")) {
    return "danger"
  }

  if (String(value || "").includes("婊氬姩浣?)) {
    return "primary"
  }

  return "info"
}

/** 鏋勯€犳瘡涓牱鏈竴鏉¤〃鏍艰褰曪細鍙睍绀鸿鏍锋湰鏈€澶х疆淇″害鏍瑰洜 */
function buildSampleSummaryRows(sourceRows) {
  const sampleIds = Object.keys(SAMPLE_ROOT_CAUSE_DISTRIBUTIONS).map(item => Number(item))

  return sampleIds.map(sampleId => {
    const sourceRow = (sourceRows || []).find(row => Number(row.sampleId) === Number(sampleId)) || {}
    const distribution = getSampleDistribution(sampleId)
    const maxItem = getMaxRootCauseBySampleId(sampleId)
    const codeSuffix = `S${String(sampleId).padStart(2, "0")}`

    return {
      ...sourceRow,
      analysisId: sampleId * 100 + 1,
      analysisCode: `RCA-${codeSuffix}-MAX`,
      diagnosisId: sourceRow.diagnosisId || sampleId + 20,
      sampleId,
      rootCauseType: maxItem.rootCauseType,
      rootCauseDesc: maxItem.rootCauseDesc,
      probability: maxItem.confidence / 100,
      evidenceJson: formatJsonText(buildEvidenceChain(codeSuffix, maxItem.confidence / 100, maxItem, distribution, sampleId)),
      maintenanceSuggestion: maxItem.maintenanceSuggestion,
      analysisMethod: sourceRow.analysisMethod || "璇婃柇缁撴灉鍏宠仈 + 鏁板瓧鍗峰畻璇佹嵁閾?+ 鐩戠鏁版嵁鎺ㄧ悊",
      analysisStatus: maxItem.confidence >= 50 ? "宸插垎鏋? : "寰呭鏍?,
      analyst: sourceRow.analyst || "Topic4-RCA-Engine",
      analysisTime: sourceRow.analysisTime || formatDateTime(new Date()),
      delFlag: "0",
      remark: `琛ㄦ牸灞曠ず璇ユ牱鏈渶澶х疆淇″害鏍瑰洜锛涘畬鏁村叚绫绘牴鍥犲垎甯冭鏌ョ湅涓婃柟楗煎浘`
    }
  })
}

/** 鏍规嵁鏌ヨ鏉′欢杩囨护鍓嶇姹囨€昏 */
function filterSummaryRows(rows) {
  return rows.filter(row => {
    if (queryParams.value.analysisCode && !String(row.analysisCode || "").includes(queryParams.value.analysisCode)) {
      return false
    }

    if (queryParams.value.diagnosisId !== null && queryParams.value.diagnosisId !== undefined && queryParams.value.diagnosisId !== "") {
      if (Number(row.diagnosisId) !== Number(queryParams.value.diagnosisId)) {
        return false
      }
    }

    if (queryParams.value.sampleId !== null && queryParams.value.sampleId !== undefined && queryParams.value.sampleId !== "") {
      if (Number(row.sampleId) !== Number(queryParams.value.sampleId)) {
        return false
      }
    }

    if (queryParams.value.rootCauseType && row.rootCauseType !== queryParams.value.rootCauseType) {
      return false
    }

    if (queryParams.value.analysisStatus && row.analysisStatus !== queryParams.value.analysisStatus) {
      return false
    }

    if (queryParams.value.analyst && !String(row.analyst || "").includes(queryParams.value.analyst)) {
      return false
    }

    if (queryParams.value.analysisTime && !String(row.analysisTime || "").startsWith(queryParams.value.analysisTime)) {
      return false
    }

    return true
  })
}

/** 鏌ヨ缃俊搴︾粺璁?*/
function getConfidenceStats() {
  buildStatsFromCurrentList()
}

/** 鐢ㄥ綋鍓嶈〃鏍兼暟鎹粺璁?*/
function buildStatsFromCurrentList() {
  const list = resultofrcList.value || []

  let highCount = 0
  let mediumCount = 0
  let lowCount = 0
  let probabilitySum = 0
  let probabilityCount = 0
  let analyzedCount = 0
  let pendingCount = 0

  list.forEach(item => {
    const probability = normalizeProbability(item.probability)

    if (item.analysisStatus === "宸插垎鏋?) {
      analyzedCount++
    }

    if (item.analysisStatus === "寰呭鏍?) {
      pendingCount++
    }

    probabilitySum += probability
    probabilityCount++

    if (probability >= 0.8) {
      highCount++
    } else if (probability >= 0.6) {
      mediumCount++
    } else {
      lowCount++
    }
  })

  confidenceStatsData.total = list.length
  confidenceStatsData.analyzedCount = analyzedCount
  confidenceStatsData.pendingCount = pendingCount
  confidenceStatsData.avgConfidence = probabilityCount > 0 ? probabilitySum / probabilityCount : 0
  confidenceStatsData.highCount = highCount
  confidenceStatsData.mediumCount = mediumCount
  confidenceStatsData.lowCount = lowCount
}

/** 褰掍竴鍖栫疆淇″害锛氬吋瀹?0.85 鍜?85 涓ょ鍐欐硶 */
function normalizeProbability(value) {
  let probability = Number(value || 0)

  if (Number.isNaN(probability) || probability < 0) {
    probability = 0
  }

  if (probability > 1) {
    probability = probability / 100
  }

  if (probability > 1) {
    probability = 1
  }

  return probability
}

/** 鑾峰彇鏌愪釜鏍锋湰鐨勫叚绫绘牴鍥犲垎甯?*/
function getSampleDistribution(sampleId) {
  const sampleNumber = Number(sampleId) || 1
  const distributionKeys = Object.keys(SAMPLE_ROOT_CAUSE_DISTRIBUTIONS).map(item => Number(item))
  const fallbackKey = distributionKeys[((sampleNumber - 1) % distributionKeys.length + distributionKeys.length) % distributionKeys.length]
  const distribution = SAMPLE_ROOT_CAUSE_DISTRIBUTIONS[sampleNumber] || SAMPLE_ROOT_CAUSE_DISTRIBUTIONS[fallbackKey] || SAMPLE_ROOT_CAUSE_DISTRIBUTIONS[1]

  return ROOT_CAUSE_LIBRARY.map(rootCause => ({
    ...rootCause,
    confidence: Number(distribution[rootCause.key] || 0)
  }))
}

function buildRootCauseDistributionByConclusion(sampleId, rootCauseType, probability) {
  const baseDistribution = getSampleDistribution(sampleId)
  const targetConfidence = Math.max(50, Math.min(90, Number(probability || 0.7) * 100))
  const otherItems = baseDistribution.filter(item => item.rootCauseType !== rootCauseType)
  const otherWeightSum = otherItems.reduce((sum, item) => sum + Number(item.confidence || 0), 0) || otherItems.length || 1
  const otherTotal = 100 - targetConfidence

  return baseDistribution.map(item => {
    if (item.rootCauseType === rootCauseType) {
      return {
        ...item,
        confidence: Number(targetConfidence.toFixed(1))
      }
    }

    return {
      ...item,
      confidence: Number((otherTotal * Number(item.confidence || 0) / otherWeightSum).toFixed(1))
    }
  })
}

/** 鑾峰彇鏍锋湰鏈€澶х疆淇″害鏍瑰洜 */
function getMaxRootCauseBySampleId(sampleId) {
  const distribution = getSampleDistribution(sampleId)

  return [...distribution].sort((a, b) => Number(b.confidence || 0) - Number(a.confidence || 0))[0]
}

/** 鍥句緥杩囬暱鏃舵埅鏂?*/
function shortChartLabel(value, maxLength = 12) {
  const text = String(value || "")

  if (text.length <= maxLength) {
    return text
  }

  return text.slice(0, maxLength) + "..."
}

/** 楗煎浘涓績鏍瑰洜鍚嶇О鎹㈣锛岄伩鍏嶉暱鏂囧瓧琚幆褰㈠浘閬尅 */
function centerRootCauseLabel(value) {
  const text = String(value || "-")

  if (text.length <= 6) {
    return text
  }

  if (text.length <= 10) {
    return `${text.slice(0, 5)}\n${text.slice(5)}`
  }

  return `${text.slice(0, 5)}\n${text.slice(5, 10)}...`
}

/** 鏋勫缓鍥捐〃涓嬫媺妗嗭細鍙樉绀烘牱鏈紪鍙?*/
function buildChartRecordOptions() {
  chartRecordOptions.value = resultofrcList.value.map(row => ({
    label: `鏍锋湰ID锛?{row.sampleId}`,
    value: String(row.sampleId)
  }))

  const exists = chartRecordOptions.value.some(item => String(item.value) === String(selectedChartSampleId.value))

  if (!exists) {
    selectedChartSampleId.value = chartRecordOptions.value.length > 0 ? chartRecordOptions.value[0].value : null
  }
}

/** 鍥捐〃璁板綍鍒囨崲 */
function handleChartRecordChange() {
  nextTick(() => {
    initConfidenceChart()
  })
}

/** 鐐瑰嚮琛ㄦ牸琛屾椂锛屽悓姝ュ垏鎹㈤ゼ鍥惧埌璇ユ牱鏈?*/
function handleTableRowClick(row) {
  selectedChartSampleId.value = String(row.sampleId)

  nextTick(() => {
    initConfidenceChart()
  })
}

/** 楂樹寒褰撳墠楗煎浘瀵瑰簲鏍锋湰 */
function tableRowClassName({ row }) {
  if (String(row.sampleId) === String(selectedChartSampleId.value)) {
    return "chart-current-row"
  }

  return ""
}

/** 鍒濆鍖栧綋鍓嶆牱鏈悇鏍瑰洜缃俊搴﹂ゼ鍥?*/
function initConfidenceChart() {
  if (!confidenceChartRef.value) {
    return
  }

  if (confidenceChartInstance) {
    confidenceChartInstance.dispose()
  }

  confidenceChartInstance = echarts.init(confidenceChartRef.value)

  if (!selectedChartSampleId.value) {
    confidenceChartInstance.setOption({
      title: {
        text: "鏆傛棤鏍锋湰鏍瑰洜鏁版嵁",
        left: "center",
        top: "center",
        textStyle: {
          color: "#7b8da3",
          fontSize: 16
        }
      },
      series: []
    })
    return
  }

  const sampleId = Number(selectedChartSampleId.value)
  const currentRow = resultofrcList.value.find(row => Number(row.sampleId) === Number(sampleId))
  const distribution = currentRow?.distribution || getSampleDistribution(sampleId)
  const maxItem = [...distribution].sort((a, b) => Number(b.confidence || 0) - Number(a.confidence || 0))[0] || getMaxRootCauseBySampleId(sampleId)

  const chartData = distribution.map(item => ({
    value: Number(item.confidence || 0),
    name: item.rootCauseType,
    row: item
  }))

  const option = {
    color: [
      "#5470c6",
      "#91cc75",
      "#fac858",
      "#ee6666",
      "#73c0de",
      "#9a60b4"
    ],
    tooltip: {
      trigger: "item",
      formatter: params => {
        const row = params.data.row

        return [
          `${params.name}`,
          `缃俊搴︼細${Number(params.value || 0).toFixed(1)}%`,
          `鏍瑰洜绫诲埆锛?{row.rootCauseCategory}`,
          `鏍锋湰ID锛?{sampleId}`
        ].join("<br/>")
      }
    },
    legend: {
      type: "scroll",
      orient: "vertical",
      right: 18,
      top: "middle",
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 12,
      formatter: name => shortChartLabel(name, 16),
      textStyle: {
        color: "#5d728c",
        fontSize: 12
      }
    },
    series: [
      {
        name: "褰撳墠鏍锋湰鍚勬牴鍥犵疆淇″害",
        type: "pie",
        radius: ["58%", "76%"],
        center: ["50%", "50%"],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: params => {
            return `${shortChartLabel(params.name, 10)}\n${Number(params.value || 0).toFixed(0)}%`
          },
          color: "#2c496b",
          fontSize: 12
        },
        labelLine: {
          show: true,
          length: 14,
          length2: 10
        },
        data: chartData
      }
    ],
    graphic: [
      {
        type: "text",
        left: "center",
        top: "middle",
        z: 20,
        style: {
          width: 150,
          text: `{label|鏈€澶ф牴鍥爙\n{name|${centerRootCauseLabel(maxItem.rootCauseType || "-")}}\n{value|${Number(maxItem.confidence || 0).toFixed(1)}%}`,
          textAlign: "center",
          textVerticalAlign: "middle",
          rich: {
            label: {
              fill: "#7b8da3",
              fontSize: 12,
              fontWeight: 600,
              lineHeight: 22
            },
            name: {
              fill: "#0c2b52",
              fontSize: 15,
              fontWeight: 800,
              lineHeight: 22
            },
            value: {
              fill: "#1d7ed0",
              fontSize: 21,
              fontWeight: 800,
              lineHeight: 28
            }
          }
        }
      }
    ]
  }

  confidenceChartInstance.setOption(option)
}

/** 鍒锋柊鍥捐〃 */
function refreshChartData() {
  getList()
}

/** 鍥捐〃灏哄閫傞厤 */
function handleChartResize() {
  if (confidenceChartInstance) {
    confidenceChartInstance.resize()
  }
}

/** 鍙栨秷 */
function cancel() {
  open.value = false
  reset()
}

/** 琛ㄥ崟閲嶇疆 */
function reset() {
  const firstRootCause = ROOT_CAUSE_LIBRARY[0]

  form.value = {
    analysisId: null,
    analysisCode: null,
    diagnosisId: null,
    sampleId: 1,
    rootCauseType: firstRootCause.rootCauseType,
    rootCauseDesc: firstRootCause.rootCauseDesc,
    probability: 0.8,
    evidenceJson: null,
    maintenanceSuggestion: firstRootCause.maintenanceSuggestion,
    analysisMethod: "璇婃柇缁撴灉鍏宠仈 + 鏁板瓧鍗峰畻璇佹嵁閾?+ 鐩戠鏁版嵁鎺ㄧ悊",
    analysisStatus: "宸插垎鏋?,
    analyst: "Topic4-RCA-Engine",
    analysisTime: formatDateTime(new Date()),
    delFlag: "0",
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }

  if (proxy.$refs["resultofrcRef"]) {
    proxy.resetForm("resultofrcRef")
  }
}

/** 鎼滅储 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 閲嶇疆鎼滅储 */
function resetQuery() {
  if (proxy.$refs["queryRef"]) {
    proxy.resetForm("queryRef")
  }
  handleQuery()
}

/** 澶氶€?*/
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.analysisId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 鏂板 */
function handleAdd() {
  reset()

  const sampleId = 1
  const maxItem = getMaxRootCauseBySampleId(sampleId)
  const distribution = getSampleDistribution(sampleId)
  const codeSuffix = `S${String(sampleId).padStart(2, "0")}`

  form.value.analysisCode = `RCA-${codeSuffix}-MAX`
  form.value.diagnosisId = sampleId + 20
  form.value.sampleId = sampleId
  form.value.rootCauseType = maxItem.rootCauseType
  form.value.rootCauseDesc = maxItem.rootCauseDesc
  form.value.probability = maxItem.confidence / 100
  form.value.evidenceJson = formatJsonText(buildEvidenceChain(codeSuffix, maxItem.confidence / 100, maxItem, distribution, sampleId))
  form.value.maintenanceSuggestion = maxItem.maintenanceSuggestion
  form.value.analysisMethod = "璇婃柇缁撴灉鍏宠仈 + 鏁板瓧鍗峰畻璇佹嵁閾?+ 鐩戠鏁版嵁鎺ㄧ悊"
  form.value.analysisStatus = "宸插垎鏋?
  form.value.analyst = "Topic4-RCA-Engine"
  form.value.analysisTime = formatDateTime(new Date())
  form.value.remark = "婕旂ず鏁版嵁锛氳〃鏍间粎灞曠ず鏍锋湰鏈€澶х疆淇″害鏍瑰洜"

  open.value = true
  title.value = "娣诲姞鏍瑰洜鍒嗘瀽缁撴灉"
}

/** 淇敼 */
function handleUpdate(row) {
  reset()

  const targetRow = row && row.analysisId ? row : resultofrcList.value.find(item => ids.value.includes(item.analysisId))

  if (!targetRow) {
    proxy.$modal.msgWarning("璇烽€夋嫨涓€鏉￠渶瑕佷慨鏀圭殑鏁版嵁")
    return
  }

  form.value = {
    ...targetRow,
    evidenceJson: formatJsonForView(targetRow.evidenceJson)
  }

  open.value = true
  title.value = "淇敼鏍瑰洜鍒嗘瀽缁撴灉"
}

/** 鎻愪氦 */
function submitForm() {
  proxy.$refs["resultofrcRef"].validate(valid => {
    if (!valid) {
      return
    }

    if (form.value.analysisId != null) {
      updateResultofrc(form.value).then(() => {
        proxy.$modal.msgSuccess("淇敼鎴愬姛")
        open.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgSuccess("婕旂ず鏁版嵁淇敼瀹屾垚")
        open.value = false
        getList()
      })
    } else {
      addResultofrc(form.value).then(() => {
        proxy.$modal.msgSuccess("鏂板鎴愬姛")
        open.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgSuccess("婕旂ず鏁版嵁鏂板瀹屾垚")
        open.value = false
        getList()
      })
    }
  })
}

/** 鍒犻櫎 */
function handleDelete(row) {
  const analysisIds = row.analysisId || ids.value

  proxy.$modal.confirm('鏄惁纭鍒犻櫎鏍瑰洜鍒嗘瀽缁撴灉缂栧彿涓?"' + analysisIds + '" 鐨勬暟鎹」锛?).then(() => {
    return delResultofrc(analysisIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("鍒犻櫎鎴愬姛")
  }).catch(() => {
    proxy.$modal.msgSuccess("婕旂ず鏁版嵁鍒犻櫎瀹屾垚")
    getList()
  })
}

/** 瀵煎嚭 */
function handleExport() {
  proxy.download("system/resultofrc/export", {
    ...queryParams.value
  }, `resultofrc_${new Date().getTime()}.xlsx`)
}

/** 璇︽儏 */
function handleDetail(row) {
  const distribution = getSampleDistribution(row.sampleId)

  detail.value = {
    ...row,
    distribution,
    rootCauseJudgment: buildRootCauseJudgment(row),
    evidenceJson: formatJsonForView(row.evidenceJson)
  }

  detailOpen.value = true
}

/** 鎵ц鏍瑰洜鍒嗘瀽 */
function handleRunRootCause() {
  const selectedRows = selectedDiagnosisRows.value || []

  if (!pipelineId.value) {
    proxy.$modal.msgWarning("鏈壘鍒板綋鍓嶆祦绋婭D锛岃鍏堜粠鏁版嵁鏂囦欢绠＄悊妯″潡寮€濮嬫祦绋?)
    return
  }

  if (!diagnosisInputList.value.length) {
    proxy.$modal.msgWarning("鏈壘鍒拌瘖鏂粨鏋滐紝璇峰厛鎵ц鏁呴殰璇婃柇")
    return
  }

  if (!selectedRows.length) {
    proxy.$modal.msgWarning("璇峰厛鍕鹃€夐渶瑕佽繘琛屾牴鍥犲垎鏋愮殑璇婃柇鏍锋湰")
    return
  }

  const analyzableRows = selectedRows.filter(row => row.faultType !== "姝ｅ父")

  if (!analyzableRows.length) {
    proxy.$modal.msgWarning("鎵€閫夋牱鏈潎涓烘甯哥姸鎬侊紝鏃犻渶鐢熸垚鏍瑰洜鍒嗘瀽缁撴灉")
    return
  }

  const rootCauseResults = analyzableRows.map((row, index) => {
    const rootCause = buildRootCauseByDiagnosis(row, index)
    const rootCauseInfo = ROOT_CAUSE_LIBRARY.find(item => item.rootCauseType === rootCause.rootCauseType) || {
      rootCauseType: rootCause.rootCauseType,
      rootCauseCategory: "缁煎悎鎺ㄧ悊",
      rootCauseDesc: rootCause.rootCauseDesc,
      maintenanceSuggestion: rootCause.maintenanceSuggestion
    }
    const distribution = buildRootCauseDistributionByConclusion(row.sampleId, rootCause.rootCauseType, rootCause.probability)

    return {
      analysisId: index + 1,
      analysisCode: `RCA-${String(row.sampleId || index + 1).padStart(3, "0")}`,
      pipelineId: pipelineId.value,
      datasetId: row.datasetId || 1,
      sampleId: row.sampleId,
      sampleCode: row.sampleCode,
      diagnosisId: row.diagnosisId,
      faultType: row.faultType,
      faultLocation: row.faultLocation,
      rootCauseType: rootCause.rootCauseType,
      rootCauseDesc: rootCause.rootCauseDesc,
      probability: rootCause.probability,
      evidenceJson: JSON.stringify(buildRootCauseEvidence(row, rootCause, distribution), null, 2),
      maintenanceSuggestion: rootCause.maintenanceSuggestion,
      analysisMethod: "璇婃柇缁撴灉鍏宠仈 + 铻嶅悎鐗瑰緛璇佹嵁閾?+ 鏍瑰洜鏈虹悊鎺ㄧ悊",
      analysisStatus: "宸插垎鏋?,
      analyst: "Topic4-RCA-Engine",
      analysisTime: formatDateTime(new Date()),
      distribution,
      remark: "鐢辫瘖鏂粨鏋滆緭鍏ョ獥鍙ｅ嬀閫夋牱鏈悗鑷姩鐢熸垚"
    }
  })

  updateTopic4Pipeline(pipelineId.value, {
    currentStage: "ROOT_CAUSE_DONE",
    status: "宸插畬鎴?,
    rootCauseResults
  })

  resultofrcList.value = filterSummaryRows(rootCauseResults)
  total.value = resultofrcList.value.length
  hasRootCauseResults.value = rootCauseResults.length > 0
  buildStatsFromCurrentList()
  buildChartRecordOptions()

  nextTick(() => {
    initConfidenceChart()
  })

  proxy.$modal.msgSuccess("鏍瑰洜鍒嗘瀽瀹屾垚锛屽凡鍦ㄤ笅鏂圭粨鏋滆褰曞拰楗煎浘涓悓姝ュ睍绀?)
}

function buildRootCauseByDiagnosis(row, index) {
  const map = {
    "鍐呭湀鏁呴殰": {
      rootCauseType: "杞存壙棰勭揣鍔涜缃亸澶?,
      rootCauseDesc: "杞存壙棰勭揣鍔涜缃亸澶э紝瀵艰嚧杩愯娓╁崌鍗囬珮銆佹帴瑙﹀簲鍔涘澶э紝骞惰鍙戝唴鍦堝尯鍩熷紓甯告尟鍔ㄣ€?,
      probability: 0.8,
      maintenanceSuggestion: "寤鸿澶嶆牳杞存壙棰勭揣鍙傛暟鍜岃閰嶅伐鑹鸿褰曪紝瀵归绱у姏鍋忓ぇ鐨勮閰嶄欢杩涜閲嶆柊璋冩暣銆?
    },
    "杞存壙鍐呭湀鏁呴殰": {
      rootCauseType: "杞存壙棰勭揣鍔涜缃亸澶?,
      rootCauseDesc: "杞存壙棰勭揣鍔涜缃亸澶э紝瀵艰嚧杩愯娓╁崌鍗囬珮銆佹帴瑙﹀簲鍔涘澶э紝骞惰鍙戝唴鍦堝尯鍩熷紓甯告尟鍔ㄣ€?,
      probability: 0.8,
      maintenanceSuggestion: "寤鸿澶嶆牳杞存壙棰勭揣鍙傛暟鍜岃閰嶅伐鑹鸿褰曪紝瀵归绱у姏鍋忓ぇ鐨勮閰嶄欢杩涜閲嶆柊璋冩暣銆?
    },
    "澶栧湀鏁呴殰": {
      rootCauseType: "瑁呴厤鍚岃酱搴﹀亸宸?,
      rootCauseDesc: "瑁呴厤鍚岃酱搴﹀亸宸鑷磋酱鎵块暱鏈熸壙鍙楀亸杞斤紝澶栧湀鎺ヨЕ鍖哄煙鍑虹幇灞€閮ㄧ柌鍔虫崯浼ゃ€?,
      probability: 0.76,
      maintenanceSuggestion: "寤鸿澶嶆牳杞存壙搴с€佽浆杞村拰绔洊瑁呴厤鍚岃酱搴︼紝瀵硅秴宸儴浠堕噸鏂板畾浣嶆牎鍑嗐€?
    },
    "杞存壙澶栧湀鏁呴殰": {
      rootCauseType: "瑁呴厤鍚岃酱搴﹀亸宸?,
      rootCauseDesc: "瑁呴厤鍚岃酱搴﹀亸宸鑷磋酱鎵块暱鏈熸壙鍙楀亸杞斤紝澶栧湀鎺ヨЕ鍖哄煙鍑虹幇灞€閮ㄧ柌鍔虫崯浼ゃ€?,
      probability: 0.76,
      maintenanceSuggestion: "寤鸿澶嶆牳杞存壙搴с€佽浆杞村拰绔洊瑁呴厤鍚岃酱搴︼紝瀵硅秴宸儴浠堕噸鏂板畾浣嶆牎鍑嗐€?
    },
    "婊氬姩浣撴晠闅?: {
      rootCauseType: "婊氶亾寰皬鎹熶激",
      rootCauseDesc: "婊氶亾寰皬鎹熶激瀵艰嚧婊氬姩浣撶粡杩囩己闄峰尯鍩熸椂浜х敓鍛ㄦ湡鎬у啿鍑伙紝閫愭褰㈡垚婊氬姩浣撳紓甯哥壒寰併€?,
      probability: 0.72,
      maintenanceSuggestion: "寤鸿瀵规粴閬撳拰婊氬姩浣撹〃闈㈣繘琛屾樉寰鏌ワ紝閲嶇偣鎺掓煡鍒掍激銆佺偣铓€鍜屽墺钀姐€?
    },
    "杞存壙婊氬姩浣撴晠闅?: {
      rootCauseType: "婊氶亾寰皬鎹熶激",
      rootCauseDesc: "婊氶亾寰皬鎹熶激瀵艰嚧婊氬姩浣撶粡杩囩己闄峰尯鍩熸椂浜х敓鍛ㄦ湡鎬у啿鍑伙紝閫愭褰㈡垚婊氬姩浣撳紓甯哥壒寰併€?,
      probability: 0.72,
      maintenanceSuggestion: "寤鸿瀵规粴閬撳拰婊氬姩浣撹〃闈㈣繘琛屾樉寰鏌ワ紝閲嶇偣鎺掓煡鍒掍激銆佺偣铓€鍜屽墺钀姐€?
    },
    "淇濇寔鏋舵晠闅?: {
      rootCauseType: "瀵嗗皝澶辨晥瀵艰嚧姹℃煋鐗╄繘鍏?,
      rootCauseDesc: "姹℃煋鐗╄繘鍏ュ悗閫犳垚娑︽粦鍔ｅ寲涓庡紓甯哥（鎹燂紝淇濇寔鏋跺尯鍩熷嚭鐜板啿鍑诲拰纾ㄦ崯鐗瑰緛銆?,
      probability: 0.7,
      maintenanceSuggestion: "寤鸿妫€鏌ュ瘑灏佸畬鏁存€с€佹鼎婊戞薄鏌撶▼搴﹀拰淇濇寔鏋剁（鎹熺姸鎬併€?
    }
  }

  return map[row.faultType] || {
    rootCauseType: "娑︽粦鑴傚～鍏呬笉瓒?,
    rootCauseDesc: "娑︽粦涓嶈冻瀵艰嚧鎽╂摝鍗囬珮鍜屽眬閮ㄧ（鎹燂紝杩涜€岃鍙戣酱鎵垮紓甯搞€?,
    probability: 0.68,
    maintenanceSuggestion: "寤鸿妫€鏌ユ鼎婊戣剛鍨嬪彿銆佸～鍏呴噺鍜岃ˉ鑴傚懆鏈熴€?
  }
}

function buildRootCauseEvidence(diagnosis, rootCause, distribution = []) {
  const rootCauseInfo = ROOT_CAUSE_LIBRARY.find(item => item.rootCauseType === rootCause.rootCauseType) || {
    rootCauseType: rootCause.rootCauseType,
    rootCauseCategory: "缁煎悎鎺ㄧ悊",
    rootCauseDesc: rootCause.rootCauseDesc,
    maintenanceSuggestion: rootCause.maintenanceSuggestion
  }
  const faultFormationProcess = buildFaultFormationProcess(rootCauseInfo, diagnosis.sampleId)

  return {
    pipelineId: pipelineId.value,
    sampleTrace: {
      sampleId: diagnosis.sampleId,
      sampleCode: diagnosis.sampleCode,
      diagnosisId: diagnosis.diagnosisId,
      fusionId: diagnosis.fusionId
    },
    diagnosisEvidence: {
      faultType: diagnosis.faultType,
      faultLocation: diagnosis.faultLocation,
      confidence: diagnosis.confidence,
      healthScore: diagnosis.healthScore
    },
    rootCauseDistribution: distribution.map(item => ({
      rootCauseType: item.rootCauseType,
      rootCauseCategory: item.rootCauseCategory,
      confidence: item.confidence
    })),
    faultFormationProcess,
    causalReasoning: faultFormationProcess.map(item => ({
      step: item.step,
      logic: item.title,
      description: item.description
    })),
    rootCauseConclusion: {
      rootCauseCategory: rootCauseInfo.rootCauseCategory,
      specificRootCause: rootCause.rootCauseType,
      rootCauseType: rootCause.rootCauseType,
      rootCauseDesc: rootCause.rootCauseDesc,
      rootCauseConfidence: rootCause.probability,
      rectificationSuggestion: rootCause.maintenanceSuggestion
    },
    reasoningChain: [
      "棰勫鐞嗘牱鏈舰鎴愭爣鍑嗗寲鏃堕棿绐?,
      "鏍锋湰澧炲己鎵╁厖鏁呴殰鏍锋湰骞舵敼鍠勭被鍒笉骞宠　",
      "鐗瑰緛铻嶅悎鎻愬彇澶氫紶鎰熷櫒鏃剁┖鐗瑰緛",
      `鏁呴殰璇婃柇璇嗗埆涓?{diagnosis.faultType}`,
      `鏍瑰洜鍒嗘瀽鎺ㄧ悊寰楀埌${rootCause.rootCauseType}`
    ]
  }
}


/** 鏋勯€犺瘉鎹摼 */
/** 鏋勯€犺瘉鎹摼锛氫粠鏍瑰洜涓€姝ヤ竴姝ユ帹瀵煎埌鏁呴殰褰㈡垚 */
function buildEvidenceChain(codeSuffix, probability, rootCauseInfo, distribution = [], sampleId = "-") {
  const faultFormationProcess = buildFaultFormationProcess(rootCauseInfo, sampleId)

  return {
    chainId: "EC-RCA-" + codeSuffix,
    sourceSubject: "璇鹃鍥?,
    targetSubject: "璇鹃浜?,
    interfaceType: "鏍瑰洜鍒嗘瀽缁撴灉杈撳嚭鎺ュ彛",

    diagnosisEvidence: {
      sourceSubject: "璇鹃鍥?,
      diagnosisCode: "DG-" + codeSuffix,
      sampleCode: "SAMPLE-" + codeSuffix,
      faultType: "杞存壙鏁呴殰",
      faultLocation: "杞存壙缁勪欢",
      diagnosisConfidence: 0.91,
      healthScore: 72.4,
      evidenceMeaning: "璇婃柇妯″瀷璇嗗埆鍒版牱鏈尟鍔ㄧ壒寰佷笌杞存壙寮傚父妯″紡楂樺害鐩稿叧銆?
    },

    digitalArchiveEvidence: {
      sourceSubject: "璇鹃涓€",
      productObject: "鑸┖瑁呭杞存壙閮ㄤ欢",
      batchNo: "BATCH-RCA-" + codeSuffix,
      bomNode: "浼犲姩绯荤粺/杞存壙缁勪欢",
      qualityFeatures: [
        "杞存壙棰勭揣鍔?,
        "婊氶亾琛ㄩ潰鐘舵€?,
        "鏉愭枡纭害涓€鑷存€?,
        "娑︽粦鑴傚～鍏呯姸鎬?,
        "瑁呴厤鍚岃酱搴?,
        "瀵嗗皝鐘舵€?
      ],
      fileReference: "bearing_sample_" + codeSuffix + ".mat",
      evidenceMeaning: "鏁板瓧鍗峰畻鎻愪緵璇ユ牱鏈殑鎵规銆侀儴浠躲€佽川閲忕壒寰佸拰鍘熷鏁版嵁鏉ユ簮銆?
    },

    supervisionEvidence: {
      sourceSubject: "璇鹃涓?,
      abnormalWarning: "鎸姩骞呭€煎紓甯?,
      equipmentStatus: "杩愯鐘舵€佸紓甯?,
      maintenanceRecord: "瀛樺湪杞存壙鍖哄煙鎸姩鍗囬珮璁板綍",
      processContext: "鐩稿悓宸ュ喌涓嬫尟鍔ㄧ壒寰佹寔缁寮?,
      evidenceMeaning: "鐩戠鏁版嵁琛ㄦ槑璇ユ牱鏈搴旇澶囧瓨鍦ㄤ笌杞存壙寮傚父涓€鑷寸殑杩愯娉㈠姩銆?
    },

    rootCauseDistribution: distribution.map(item => ({
      rootCauseType: item.rootCauseType,
      rootCauseCategory: item.rootCauseCategory,
      confidence: item.confidence
    })),

    /**
     * 鏂板锛氭晠闅滃舰鎴愯繃绋?
     * 琛ㄦ牸涓殑鈥滆瘉鎹摼鎽樿鈥濆氨浠庤繖閲屽彇鍊笺€?
     */
    faultFormationProcess,

    causalReasoning: faultFormationProcess.map(item => ({
      step: item.step,
      logic: item.title,
      description: item.description
    })),

    rootCauseConclusion: {
      rootCauseCategory: rootCauseInfo.rootCauseCategory,
      specificRootCause: rootCauseInfo.rootCauseType,
      rootCauseType: rootCauseInfo.rootCauseType,
      rootCauseDesc: rootCauseInfo.rootCauseDesc,
      rootCauseConfidence: probability,
      impactScope: "鍚屾壒娆¤酱鎵跨粍浠跺強鐩歌繎宸ュ喌杩愯璁惧",
      responsibleStage: rootCauseInfo.rootCauseCategory,
      rectificationSuggestion: rootCauseInfo.maintenanceSuggestion
    }
  }
}

function parseEvidenceJson(value) {
  if (!value) {
    return {}
  }

  if (typeof value === "object") {
    return value
  }

  try {
    return JSON.parse(value)
  } catch (error) {
    return {}
  }
}

function buildEvidenceView(row) {
  if (!row) {
    return {
      sampleCode: "-",
      steps: [
        { title: "璇婃柇杈撳叆", primary: "鏆傛棤鏍锋湰", desc: "璇峰厛鎵ц鏍瑰洜鍒嗘瀽骞堕€夋嫨鏍锋湰銆?, type: "input" },
        { title: "寮傚父鐗瑰緛", primary: "寰呯敓鎴?, desc: "鎵ц鍚庡睍绀洪璋便€佽瀺鍚堢壒寰佸拰鐩戠璇佹嵁銆?, type: "feature" },
        { title: "鍊欓€夋牴鍥?, primary: "寰呯敓鎴?, desc: "鏍规嵁鏍瑰洜鍒嗗竷鐢熸垚鍊欓€夋牴鍥犳帓搴忋€?, type: "candidate" },
        { title: "鏈€澶ф牴鍥?, primary: "寰呯敓鎴?, desc: "灞曠ず缃俊搴︽渶楂樼殑鍏蜂綋鏍瑰洜銆?, type: "cause" },
        { title: "鏁存敼寤鸿", primary: "寰呯敓鎴?, desc: "杈撳嚭缁翠慨涓庡鏍稿缓璁€?, type: "action" }
      ],
      formation: []
    }
  }

  const evidence = parseEvidenceJson(row.evidenceJson)
  const diagnosis = evidence.diagnosisEvidence || {}
  const archive = evidence.digitalArchiveEvidence || {}
  const supervision = evidence.supervisionEvidence || {}
  const conclusion = evidence.rootCauseConclusion || {}
  const distribution = evidence.rootCauseDistribution || row.distribution || getSampleDistribution(row.sampleId)
  const formation = evidence.faultFormationProcess || buildFaultFormationProcess({ rootCauseType: row.rootCauseType }, row.sampleId)

  const candidates = [...distribution]
      .sort((a, b) => Number(b.confidence || 0) - Number(a.confidence || 0))
      .slice(0, 3)
      .map(item => `${item.rootCauseType} ${Number(item.confidence || 0).toFixed(0)}%`)
      .join(" / ")

  const featureEvidence = [
    diagnosis.evidenceMeaning || `${row.faultLocation || "杞存壙缁勪欢"}璇嗗埆涓?{row.rootCauseType || "寮傚父鏍瑰洜"}鐩稿叧鏁呴殰`,
    supervision.abnormalWarning ? `鐩戠寮傚父锛?{supervision.abnormalWarning}` : "鐩戠寮傚父锛氭尟鍔ㄥ箙鍊煎紓甯?,
    archive.fileReference ? `鏍锋湰鏂囦欢锛?{archive.fileReference}` : `鏍锋湰缂栧彿锛?{row.sampleCode || `SAMPLE-${row.sampleId}`}`
  ].join("锛?)

  return {
    sampleCode: row.sampleCode || `SAMPLE-${row.sampleId || "-"}`,
    steps: [
      {
        title: "璇婃柇杈撳叆",
        primary: `${diagnosis.faultLocation || row.faultLocation || "杞存壙缁勪欢"} / ${diagnosis.faultType || "杞存壙鏁呴殰"}`,
        desc: `璇婃柇缃俊搴?${formatProbability(diagnosis.diagnosisConfidence ?? row.probability)}锛屽仴搴疯瘎鍒?${diagnosis.healthScore || row.healthScore || "72.4"}`,
        type: "input"
      },
      {
        title: "寮傚父鐗瑰緛",
        primary: "鎸姩寮傚父 + 铻嶅悎鐗瑰緛鍋忕",
        desc: featureEvidence,
        type: "feature"
      },
      {
        title: "鍊欓€夋牴鍥?,
        primary: candidates || "鍊欓€夋牴鍥犲垎甯冨緟鐢熸垚",
        desc: "鏍规嵁璇婃柇缁撴灉銆佽瀺鍚堢壒寰佸拰鏁板瓧鍗峰畻璇佹嵁鐢熸垚鍏被鏍瑰洜缃俊搴︽帓搴忋€?,
        type: "candidate"
      },
      {
        title: "鏈€澶ф牴鍥?,
        primary: conclusion.specificRootCause || row.rootCauseType || "-",
        desc: `鏈€澶ф牴鍥犵疆淇″害 ${formatProbability(conclusion.rootCauseConfidence ?? row.probability)}锛岃矗浠婚樁娈碉細${conclusion.responsibleStage || "瑁呴厤 / 鏉愭枡 / 娑︽粦缁煎悎澶嶆牳"}`,
        type: "cause"
      },
      {
        title: "鏁存敼寤鸿",
        primary: "澶嶆牳骞堕棴鐜鐞?,
        desc: conclusion.rectificationSuggestion || row.maintenanceSuggestion || "寤鸿缁撳悎瑁呴厤璁板綍銆佹鼎婊戠姸鎬佸拰鍚屾壒娆¤川閲忔暟鎹繘琛屽鏍搞€?,
        type: "action"
      }
    ],
    formation: formation.slice(0, 6)
  }
}

/** 鏋勫缓鏍瑰洜鍒ゆ柇鏂囨湰 */
function buildRootCauseJudgment(row) {
  const specificRootCause = row.rootCauseType || "-"
  const desc = row.rootCauseDesc || "-"

  return `${specificRootCause}锛?{desc}`
}

/** 琛ㄦ牸涓樉绀鸿瘉鎹摼鎽樿锛氭牴鍥?鈫?鏈虹悊婕斿寲 鈫?鏁呴殰褰㈡垚 */
function buildEvidenceSummary(value) {
  if (!value) {
    return "鏆傛棤璇佹嵁閾?
  }

  try {
    const json = typeof value === "string" ? JSON.parse(value) : value
    const process = json.faultFormationProcess || []

    if (process.length > 0) {
      return process.map(item => item.title).join(" 鈫?")
    }

    const reasoning = json.causalReasoning || []

    if (reasoning.length > 0) {
      return reasoning.map(item => item.logic || item.description).join(" 鈫?")
    }

    const specificRootCause = json.rootCauseConclusion?.specificRootCause || "鏈€澶х疆淇″害鏍瑰洜"

    return `${specificRootCause} 鈫?灞€閮ㄥ紓甯哥疮绉?鈫?鎸姩鐗瑰緛澧炲己 鈫?杞存壙鏁呴殰褰㈡垚`
  } catch (e) {
    return "鏍瑰洜寮傚父 鈫?灞€閮ㄦ崯浼ょ疮绉?鈫?鎸姩鍝嶅簲澧炲己 鈫?杞存壙鏁呴殰褰㈡垚"
  }
}

/** 鏍瑰洜绫诲瀷鏍囩 */
function rootCauseTypeTag(value) {
  const rootCause = ROOT_CAUSE_LIBRARY.find(item => item.rootCauseType === value)

  if (!rootCause) {
    return "info"
  }

  const categoryMap = {
    "瑁呴厤鍙傛暟": "warning",
    "琛ㄩ潰鎹熶激": "danger",
    "鏉愭枡涓€鑷存€?: "danger",
    "娑︽粦缁存姢": "success",
    "瑁呴厤宸ヨ壓": "warning",
    "鐜姹℃煋": "info"
  }

  return categoryMap[rootCause.rootCauseCategory] || "info"
}

/** 鐘舵€佹爣绛?*/
function analysisStatusTag(value) {
  if (value === "宸插垎鏋?) {
    return "success"
  }

  if (value === "寰呭鏍?) {
    return "warning"
  }

  if (value === "寰呭垎鏋?) {
    return "info"
  }

  return "info"
}

/** 鏍煎紡鍖栫疆淇″害 */
function formatProbability(value) {
  if (value === null || value === undefined || value === "") {
    return "-"
  }

  const num = Number(value)

  if (Number.isNaN(num)) {
    return value
  }

  if (num <= 1) {
    return (num * 100).toFixed(1) + "%"
  }

  return num.toFixed(1) + "%"
}

/** JSON鏍煎紡鍖?*/
function formatJsonText(value) {
  return JSON.stringify(value, null, 2)
}

/** 璇︽儏涓牸寮忓寲JSON */
function formatJsonForView(value) {
  if (!value) {
    return "鏆傛棤鏁版嵁"
  }

  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch (e) {
    return value
  }
}

/** 鏃ユ湡鏃堕棿鏍煎紡 */
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

onMounted(() => {
  loadPipelineInput()
  getList()
  window.addEventListener("resize", handleChartResize)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleChartResize)

  if (confidenceChartInstance) {
    confidenceChartInstance.dispose()
    confidenceChartInstance = null
  }
})
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

.table-card,
.interface-card,
.confidence-overview,
.rootcause-input-card {
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);
}

.confidence-overview {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(420px, 0.9fr);
  gap: 18px;
  align-items: stretch;
}

.stats-only-overview {
  grid-template-columns: 1fr;
}

.section-title-row,
.chart-header,
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

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.metric-mini {
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 16px;
  background: #ffffff;

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

.rootcause-input-card {
  overflow: hidden;
}

.rootcause-input-header {
  align-items: flex-start;
}

.section-desc {
  margin: 6px 0 0;
  color: #5d728c;
  font-size: 13px;
  line-height: 1.7;
}

.rootcause-input-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: stretch;
}

.input-table-panel,
.input-action-panel {
  min-width: 0;
  padding: 16px 18px;
  border: 1px solid #d6e7f7;
  border-radius: 16px;
  background: #ffffff;
}

.panel-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;

  h4 {
    margin: 0;
    color: #0c2b52;
    font-size: 16px;
    font-weight: 800;
  }

  p {
    margin: 6px 0 0;
    color: #6b7f99;
    font-size: 12px;
    line-height: 1.6;
  }
}

.panel-title-row.compact {
  margin-bottom: 12px;
}

.input-summary-list {
  display: grid;
  gap: 10px;
}

.summary-box {
  padding: 12px 14px;
  border: 1px solid #dbeaf8;
  border-radius: 12px;
  background: #f6faff;

  span {
    display: block;
    margin-bottom: 6px;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    display: block;
    color: #0c2b52;
    font-size: 14px;
    font-weight: 800;
    line-height: 1.5;
    word-break: break-all;
  }
}

.run-rootcause-btn {
  width: 100%;
  height: 40px;
  margin-top: 16px;
  font-weight: 800;
}

.rootcause-chart-panel {
  margin-top: 16px;
  padding: 16px 18px 12px;
  border: 1px solid #d6e7f7;
  border-radius: 16px;
  background: #ffffff;
  min-width: 0;
  overflow: hidden;
}

.rootcause-inline-chart {
  height: 420px;
}

.chart-card {
  height: 100%;
  padding: 4px 4px 0;
}

.confidence-chart {
  width: 100%;
  height: 420px;
}

.chart-legend-note {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  color: #6b7f99;
  font-size: 12px;
  line-height: 1.6;
}

.evidence-chain-panel {
  margin-top: 18px;
  padding: 16px 18px;
  border: 1px solid #dbe9f7;
  border-radius: 16px;
  background: #ffffff;
}

.evidence-chain-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;

  h4 {
    margin: 0;
    color: #0c2b52;
    font-size: 18px;
    font-weight: 900;
  }

  p {
    margin: 6px 0 0;
    color: #6b7f99;
    font-size: 13px;
    line-height: 1.6;
  }
}

.evidence-flow-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) 26px minmax(0, 1.15fr) 26px minmax(0, 1.18fr) 26px minmax(0, 1.1fr) 26px minmax(0, 1.1fr);
  align-items: stretch;
  gap: 8px;
}

.evidence-flow-card {
  display: flex;
  gap: 10px;
  min-height: 126px;
  padding: 14px;
  border: 1px solid #d8e8f8;
  border-radius: 14px;
  background: #f8fbff;
  box-shadow: 0 8px 20px rgba(31, 95, 160, 0.05);
}

.evidence-flow-card.feature {
  background: #fffaf2;
  border-color: #f5dfb8;
}

.evidence-flow-card.candidate {
  background: #f7fff5;
  border-color: #cfe9c9;
}

.evidence-flow-card.cause {
  background: #fff7f7;
  border-color: #f4c7c7;
}

.evidence-flow-card.action {
  background: #f7f4ff;
  border-color: #d8cef7;
}

.evidence-step-index {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 28px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #3a8ee6, #1f6fd1);
  font-size: 13px;
  font-weight: 900;
}

.evidence-step-main {
  min-width: 0;

  span {
    display: block;
    color: #63809d;
    font-size: 12px;
    font-weight: 800;
  }

  strong {
    display: block;
    margin-top: 6px;
    color: #0c2b52;
    font-size: 14px;
    line-height: 1.45;
    font-weight: 900;
  }

  p {
    margin: 8px 0 0;
    color: #4f647f;
    font-size: 12px;
    line-height: 1.65;
  }
}

.evidence-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #76a6d9;
  font-size: 22px;
  font-weight: 900;
}

.fault-formation-chain {
  margin-top: 14px;
  padding: 12px 14px;
  border: 1px dashed #cbdff4;
  border-radius: 14px;
  background: #fbfdff;
}

.formation-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 13px;
  font-weight: 900;
}

.formation-steps {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.formation-pill {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 5px 10px;
  border-radius: 999px;
  color: #244568;
  background: #eef6ff;
  border: 1px solid #d5e7f9;
  font-size: 12px;
  font-weight: 700;
}

.formation-arrow {
  color: #7ea7d2;
  font-weight: 900;
}

.interface-title {
  margin-bottom: 12px;
  color: #0c2b52;
  font-size: 18px;
  font-weight: 800;
}

.interface-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.interface-item {
  padding: 14px 16px;
  border: 1px solid #d6e7f7;
  border-radius: 12px;
  background: #f8fbff;

  span {
    display: block;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    display: block;
    margin-top: 6px;
    color: #0c2b52;
    font-size: 14px;
    font-weight: 800;
  }
}

.table-card-header {
  margin-bottom: 12px;
}

.table-filter-panel {
  margin-bottom: 14px;
  padding: 14px 16px 2px;
  border: 1px solid #d6e7f7;
  border-radius: 14px;
  background: #ffffff;
}

.table-filter-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 15px;
  font-weight: 800;
}

.table-filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.interface-collapse-card {
  padding: 10px 18px;
  background: #ffffff;
}

.interface-collapse-card :deep(.el-collapse) {
  border: none;
}

.interface-collapse-card :deep(.el-collapse-item__header) {
  border: none;
  color: #0c2b52;
  font-size: 16px;
  font-weight: 800;
}

.interface-collapse-card :deep(.el-collapse-item__wrap) {
  border: none;
}

.interface-collapse-card :deep(.el-collapse-item__content) {
  padding-bottom: 10px;
}

.mb8 {
  margin-bottom: 14px;
}

.judgment-cell {
  line-height: 1.6;
}

.judgment-desc {
  margin-top: 6px;
  color: #4f647f;
  font-size: 13px;
}

.evidence-summary {
  color: #334e6f;
  line-height: 1.7;
}

.confidence-text {
  color: #0c74d5;
  font-weight: 800;
}

.sample-chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
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

:deep(.el-table__row.chart-current-row > td.el-table__cell) {
  background: #eaf5ff !important;
}

:deep(.pagination-container) {
  margin-top: 18px;
  background: transparent;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__title) {
  font-weight: 800;
  color: #0c2b52;
}

@media screen and (max-width: 1400px) {
  .confidence-overview {
    grid-template-columns: 1fr;
  }

  .rootcause-input-grid {
    grid-template-columns: 1fr;
  }

  .evidence-flow-grid {
    grid-template-columns: 1fr;
  }

  .evidence-arrow {
    transform: rotate(90deg);
    min-height: 24px;
  }

  .interface-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 900px) {
  .metric-grid,
  .interface-grid {
    grid-template-columns: 1fr;
  }

  .module-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}
</style>
