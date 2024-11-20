
## 1. 主要類別結構
- `Batter`: 打者資料類別，儲存打擊數據
- `PitchResult`: 投球結果類別，記錄起始和終點位置
- `PitchType`: 球種資料類別，記錄球速和轉速範圍
- `PitchStrategy`: 投球策略類別，決定投球方式
- `PitchStrategyTest`: 測試類別，驗證系統功能




## 主要類別結構

### 1. Batter（打者資料）
- 儲存投球和安打數據的映射（Map）
- 計算各區域打擊率
- 例如：5號位置 185次投球、41次安打

### 2. PitchResult（投球結果）
- 記錄投球的起始和終點位置
- 判斷好球和壞球區
- 分析投球軌跡（直球、變化球等）

### 3. PitchType（球種資料）
- 記錄九種不同球種特性
- 儲存速度範圍（KPH）
- 儲存轉速範圍（RPM）

### 4. PitchStrategy（投球策略）
- 決定投球路徑
- 處理球種選擇
- 根據打者數據制定策略

### 5. PitchStrategyTest（測試類別）
- 驗證系統各項功能
- 測試覆蓋率：54%
- 包含多種測試場景

## 2. 核心功能測試
```java
// 打擊熱區分析
testOhtaniHotZones()

// 球種策略測試
testPitchTypes()

// 特殊情況處理
testEqualBattingAverages()
testInvalidZoneIdentifier()

// Count策略測試
testPitchCountStrategies()
```

## 3. 測試覆蓋率
```
覆蓋率統計：
- Instructions: 54% 覆蓋
- Branches: 66% 覆蓋
- Lines: 204行中112行已覆蓋
- Methods: 56個中24個已覆蓋
- Classes: 5個中4個已覆蓋
```

## 1. 覆蓋率報告分析
```
Element (org.example) 的覆蓋率統計：
- Missed Instructions: 46% 覆蓋率
- Missed Branches: 34% 覆蓋率
- Lines: 204 行中有 92 行未覆蓋
- Methods: 56 個中有 32 個未覆蓋
- Classes: 5 個中有 1 個未覆蓋
```

## 2. 主要測試類別結構
- `PitchStrategyTest` 包含多種測試方法
- 使用 `@BeforeEach` 初始化測試數據
- 使用 `@DisplayName` 提供測試說明

## 3. 核心測試案例
1. **球種資料測試**
   - 測試九種不同球種
   - 驗證速度和轉速範圍

2. **打擊熱區分析**
   - 測試大谷翔平的打擊數據
   - 驗證起始和終點區域

3. **特殊情況處理**
   - 相同打擊率的處理
   - 非法區域標識的處理
   - 空數據的處理

4. **Count策略測試**
   - 不同球數組合的測試
   - 好球壞球的判斷邏輯

## 4. 測試失敗分析
根據錯誤訊息：
```
org.opentest4j.AssertionFailedError: 
起始區域應為有效區域 ==> 
Expected: true
Actual: false
```
顯示在非法區域標識測試中有驗證失敗的情況。





