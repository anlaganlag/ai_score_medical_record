**首先我需要该项目时TDD方式开发的,至少要包括单元测试** 


其次我补充了一些关键信息来编写完整的PRD文档。你可以继续提出需要补充的关键信息

## 技术架构相关问题

### 1. **HMS住院系统API接口**
- HMS系统的API接口文档地址或示例？

接口1获取病人信息:
        http://172.16.1.13:10086/admin-api/business/patient/get?id=225


headers:
    Authorization  Bearer d440578b-5ab3-4254-adf9-d9527503ae84
    systemdeptid 100
返回的response:
    {
    "code": 0,
    "data": {
        "createTime": "2024-09-12 16:34:35",
        "updateTime": "2025-07-03 12:32:39",
        "creator": "156",
        "updater": "149",
        "deleted": false,
        "deptId": 100,
        "id": 225,
        "patientType": "1",
        "name": "叶金铃",
        "spellName": "yejinling",
        "nickName": null,
        "dialyzeNo": "02050002",
        "sex": "2",
        "idCardType": "2",
        "idCard": "441721199507053560",
        "birthday": "1995-07-05",
        "age": 29,
        "patientSource": "1",
        "hospitalNo": "102050002",
        "endemicArea": null,
        "bedNo": null,
        "marriage": "1",
        "applyWay": "6",
        "baoNo": null,
        "baoEndTime": "2025-05-07",
        "baoEndWarnTime": "2025-04-01",
        "stature": "165",
        "initWeight": "47.8",
        "bloodType": null,
        "rh": null,
        "culture": "172",
        "occupation": "168",
        "smoking": "无",
        "eyesight": "无",
        "tipple": "无",
        "religion": null,
        "nation": "1",
        "mobile": "17260189268",
        "mobileMan": "13169144280",
        "address": "阳西县织篢镇粤丰广场",
        "workUnit": "无",
        "receiveTime": "2023-01-09",
        "firstReceiveTime": "2017-08-18",
        "dialyzeAge": "7年零10月",
        "induceTime": null,
        "initDialyzeNo": "0",
        "dialyzeTotal": "136",
        "infect": "",
        "medic": null,
        "nurse": 149,
        "describes": null,
        "remark": null,
        "allergy": "诉地塞米松及阿莫西林过敏",
        "tumor": "无",
        "erythrocyte": null,
        "authRole": null,
        "authName": null,
        "authIdCard": null,
        "authMobile": null,
        "avatar": "http://172.16.1.13:10086/upimages/2024/09/12/286179ba-f299-465d-8a0e-6f23e809b0d7.jpg",
        "labels": null,
        "rfid": null,
        "postcode": null,
        "openId": null,
        "state": "0",
        "patientState": null,
        "adjustNumber": "9",
        "hemodialysis": null,
        "hemofiltration": null,
        "perfusion": null,
        "enterWay": null,
        "carryState": null,
        "signature": null,
        "patientTypeSource": "00",
        "cnrdsSyncFlag": "Y",
        "teamCount": null,
        "weekCount": null,
        "monthCount": null,
        "facilityName": "A15",
        "adjustFacilityName": null,
        "classesState": null,
        "prescriptionState": null,
        "dialyzeName": null,
        "dialyzeValue": null,
        "scheduling": null,
        "dialysisPlan": "1",
        "sort": null,
        "dateList": null,
        "diseaseReasonNames": "维持性血透状态,慢性肾小球肾炎,慢性肾功能不全（CKD-5期）,肾性贫血,肾性高血压,肾性骨病,高磷酸血症,继发性甲状旁腺功能亢进,睡眠障碍,血栓性静脉炎,高β2微球蛋白血症",
        "classesTime": null,
        "facilityId": null,
        "dayState": null,
        "weekState": null,
        "useRegisterId": null,
        "startDialyzeTime": "2025-07-03 13:12:00",
        "endDialyzeTime": "2025-07-03 17:12:00",
        "disinfectTime": null,
        "diagnosis": ",,,,,,,",
        "weightPre": null,
        "pressurePre": null,
        "firstCureTime": null,
        "lastCureTime": null,
        "outTime": null,
        "latestReceiveTime": null,
        "modelConsumableSpec": null,
        "finishDialyzeTime": null,
        "enabledMark": null,
        "nurseName": null,
        "disease": null
    },
    "msg": ""
}

接口2获取诊疗信息:
    http://172.16.1.13:10086/admin-api/business/first-course-record/get
payloady:
    {"patientId":225}
headers和上一个接口一样
response:
    {
    "code": 0,
    "data": {
        "patientId": 225,
        "patientName": null,
        "patientNickName": null,
        "dialyzeNo": null,
        "dialyzeAge": null,
        "spellName": null,
        "id": 1693,
        "startTime": "2023-01-10 00:00:00",
        "mainSuit": "维持血透5年余。",
        "nowDiseaseHistory": "患者于2017年8月因恶心、气喘,在南方医院查血肌酐、尿素氮升高，诊断为CKD5期，于左前臂造瘘，开始维持血透治疗3次/周，经治疗恶心、气喘症状好转，先后到阳西人民医院、阳东养可维持血透治疗,现静注促红5000u，3次/周，目前血红蛋白约130g/l；口服骨化三醇0.5ug/d治疗肾性骨病，目前PTH600pg/ml左右；间断口服药物 治疗高血压，目前血压控制尚好。近半年因肝素相关性血小板减少症,行无肝素透析.无胸闷气促、无水肿，无皮肤瘙痒,无出血。今转入我中心继续维持血透治疗。现患者无一般情况尚好，无乏力，大便正常，无尿，睡眠好，干体重47.8kg。\n既往史：否认糖尿病，高血压病史，否认肝炎、结核等传染病史。2015年因闭经在阳西人民医院诊断：垂体瘤予溴隐亭治疗2年，停药后未复发。有地塞米松、阿莫西林过敏史，贝朗透析器H系列过敏史，无外伤史，手术史：2017年8月左前臂造瘘，无输血史。\n个人史：生于阳西县，未到外地居住，不吸烟，不饮酒。未接种新冠疫苗，2022年12月27日发热，查新冠抗原阳性 。\n月经生育史：未婚，初潮年龄14，月经期3-5天/周期28天，量正常。\n家族史：父母健在，兄弟姐妹无同样病者，家族中无传染病及家族性遗传病史。",
        "oldDiseaseHistory": "否认糖尿病，高血压病史，否认肝炎、结核等传染病史。2015年因闭经在阳西人民医院诊断：垂体瘤予溴隐亭治疗2年，停药后未复发。有地塞米松、阿莫西林过敏史，无外伤史，手术史：2017年8月左前臂造瘘，无输血史。",
        "bodyInspect": "一般情况：体型(正常),发育(正常),入科(步行),神志(清楚),体位(自主),步态(正常),查体(合作),语言(流利),病容(慢性)\n皮肤及淋巴结：皮肤弹性(正常),皮疹(无),皮下出血(无),瘀斑(无),蜘蛛痣(无),肝掌(无),瘢痕(无),溃疡(无)\n头颈部五官：头颅：大小形状((正常)),毛发((正常)),眼((角膜透明),(瞳孔等大等圆)(左3mm,右3mm),(对光反射存在)),耳：外形((正常)),听力((正常)),鼻((正常)),口((舌居中)),颈((颈部对称),(活动自如),(气管居中))\n胸部：胸部(胸廓对称),肛门直肠及外生殖器(未检),肺：视诊((胸式呼吸)),肺：触诊：呼吸活动度((一致)),肺：叩诊：肺部叩诊((清音)),肺：听诊：双肺呼吸音((清晰)),心脏：视诊((心尖搏动位于锁骨中线第)(5肋间),(内/外)(0.5cm)),心脏：触诊：心尖搏动((有力)),心脏：叩诊：心脏相对浊音界((正常))\n腹部：视诊：腹部((平坦)),触诊：腹壁((柔软))\n脊柱及四肢：脊柱(正常),活动(自如,关节活动自如),上肢动静脉内瘘(上肢动静脉内瘘),震颤(有),杂音(有)\n神经系统：生理反射(肱二头肌、肱三头肌反射正常,膝腱反射正常,跟腱反射正常),病理反射(未引出),脑膜刺激征(未引出)",
        "identifyDiagnosis": "诊断明确，无需鉴别。",
        "healingProject": "低盐低钾低脂、高蛋白饮食，纠正贫血，调整血压改善肾功能对症治疗；规律透析，每周3次；定期复查；血常规、肾功能、传染病等相关检查。",
        "initDiagnosis": "维持性血透状态,慢性肾小球肾炎,慢性肾功能不全（CKD-5期）,肾性贫血,肾性高血压,肾性骨病,高磷酸血症,继发性甲状旁腺功能亢进",
        "signature": 162,
        "remark": null,
        "assayInspect": "HBsAg【-】，HBsAb【-】，HBeAg【-】，HBeAb【-】，HBcAb【-】，丙肝抗体【-】，梅毒TP【-】，HIV抗体【-】。"
    },
    "msg": ""
}

AI需要提取上述相关信息进行打分的输入,生成固定格式的AI打分报告


- 基本病历信息API的具体字段有哪些？
主要分为8类
 1. 主诉
    - describes : 病情描述
 2. 病史
    - allergy : 过敏史
    - tumor : 肿瘤史
    - smoking : 吸烟史
    - tipple : 饮酒史
    - infect : 传染病史
    - dialyzeAge : 透析龄
    - firstReceiveTime : 首次透析时间
    - receiveTime : 建档时间
    - firstCureTime : 首次治疗时间
    - lastCureTime : 末次治疗时间
    - dialyzeTotal : 透析总次数
 3. 体格检查
    - stature : 身高
    - initWeight : 初始体重
    - weightPre : 透析前体重
    - pressurePre : 透析前血压
    - eyesight : 视力
 4. 辅助检查
    - bloodType : 血型
    - rh : Rh血型
    - erythrocyte : 红细胞
 5. 诊断
    - diagnosis : 诊断
    - disease : 疾病
    - diseaseReasonNames : 原发病
 6. 处理
    - dialysisPlan : 透析计划
    - medic : 医生
    - nurse : 护士
    - nurseName : 护士姓名
    - facilityName : 床位/设备名称
    - hemodialysis : 血液透析
    - hemofiltration : 血液滤过
    - perfusion : 血液灌流
    - startDialyzeTime : 开始透析时间
    - endDialyzeTime : 结束透析时间
 7. 总体评价
    - state : 患者状态
    - patientState : 患者状态详情
 8. 其他
    - 基本信息 : name , sex , age , birthday , idCard , mobile , address , marriage , occupation , nation , culture
    - 患者标识 : id , deptId , patientType , dialyzeNo , hospitalNo , avatar
    - 业务相关 : patientSource , applyWay , baoNo , baoEndTime , remark , cnrdsSyncFlag , sort , enabledMark
    - 联系人信息 : mobileMan , authName , authMobile , authIdCard
    - 排班与状态 : classesState , prescriptionState , scheduling , dayState , weekState
    - 其他时间 : finishDialyzeTime , outTime , disinfectTime
    
- 诊疗信息API的具体字段有哪些？
    取初步诊断字段即initDiagnosis,如 
        "initDiagnosis": "维持性血透状态,慢性肾小球肾炎,慢性肾功能不全（CKD-5期）,肾性贫血,肾性高血压,肾性骨病,高磷酸血症,继发性甲状旁腺功能亢进",

- API调用频率限制？
    RPM	 1,000 
    TPM  50,000

### 2. **DeepSeek模型集成**
- DeepSeek模型的调用方式（REST API、SDK等）？
    调用硅基的api


- 模型调用的认证信息如何配置？
    url:https://api.siliconflow.cn/v1/chat/completions
    api-key:sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv
    模型名称:deepseek-ai/DeepSeek-R1-0528-Qwen3-8B
- 模型输入格式要求（JSON结构、字段映射）？
    输入JSON
- 模型响应时间预期？
    不做限制
- 模型调用失败?
    重试

### 3. **系统集成**
- 现有SpringBoot项目的版本和主要技术栈？
     核心框架 : Spring Boot 3.2.5
     Java 版本 : JDK 17
     Web 框架 : Spring Web (MVC)
     数据持久化 : Spring Data JPA, Hibernate
     数据库驱动 : MySQL Connector/J
     缓存 : Redisson (用于与 Redis 交互)
     认证与授权 : Sa-Token
     工具库 : Lombok, Apache Commons Lang3, Hutool
     日志 : Logstash Logback Encoder
     监控 : Spring Boot Actuator
     校验 : Spring Boot Validation
     测试 : Spring Boot Starter Test, H2 Database
- 现有Vue前端项目的版本（Vue2/Vue3）？
    Vue2
- 数据库类型和版本（MySQL、PostgreSQL等）？
    MySQL8



## 业务流程相关问题

### 4. **用户角色和权限**
- 打分结果是否需要专家点评,即病历和专家点评需要需要数据存储

### 5. **数据处理**
- 病历打分是实时进行还是批量处理？
    当前是点击按钮"AI病历质控生成/更新"触发数据处理,点击按钮"AI病历质控报告查看"查看报告

### 6. **前端展示**
- 打分结果展示页面的具体要求？
    写死前端展示页面,可以参照 "评分报告"文件夹里的"胡安秀病历评分报告.md"作为写死的输出报告




### 8. **业务规则说明**
- 日志记录要求？
    需要再关键节点记录完整的日志,便于AI能排查bug和修复重新跑通测试代码


请您提供这些信息，我将为您编写详细的PRD文档。