# 锐曼SDK接入指南
[toc]
## BaseBigManDemo项目下载地址
> https://github.com/yebook/BaseBigMan             //4.4系统

> https://github.com/yebook/BaseBigMan3399     //6.0系统


## 添加权限

> 在清单文件中添加相关权限 

    <!-- 访问网络 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 	<!-- SD卡读取权限，用户写入离线定位数据 -->
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

	<!-- 这个权限用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!-- 这个权限用于访问GPS定位 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <!-- 这个权限用于获取wifi的获取权限，wifi信息会用来进行网络定位 -->
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
	<!-- 相机权限，人脸识别 -->
	<uses-permission android:name="android.permission.CAMERA" />
	<uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-feature android:name="android.hardware.camera2.full" />


## 导入包

* 1.将SDK包中的libs文件拷贝至Android工程的libs目录下
* 2.将app module下的gradle配置文件中指定默认jniLibs目录为libs

		apply plugin: 'com.android.application'
		
		android {
		    compileSdkVersion 26
		    defaultConfig {
		        applicationId "com.reemanye.selectitemai"
		        minSdkVersion 19
		        targetSdkVersion 26
		        versionCode 1
		        versionName "1.0"
		        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
		    }
		    buildTypes {
		        release {
		            minifyEnabled false
		            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
		        }
		    }
			
			//在自己项目中添加以下代码
		    sourceSets {
		        main {
		            jniLibs.srcDirs = ['libs']
		        }
		    }
		}

## 集成AI语音聊天模块

* 1.在Application中初始化，将其中ID替换为自己的ID
	`SpeechPlugin.CreateSpeechUtility(this, "讯飞语音ID", "锐曼语料后台应用ID");`
* 2.（注意）将讯飞的msc.jar包以及相关的libmsc.so库文件放入项目中，如果出现能识别但不能合成的情况，请将demo中的msc.jar包复制到自己项目下(libmsc.so以及id使用自己的即可)
* 3.初始化语音处理回调

		SpeechPlugin.CreateInstance(this);  //init ai
        SpeechPlugin.getInstance().setDevID(RobotActionProvider.getInstance().getRobotID());
        SpeechPlugin.getInstance().setRecognizeListener(new SpeechRecoProcess());  // 设置语音识别处理
        SpeechPlugin.getInstance().setResultProcessor(new SpeechResultProcess());    // 设置AI语料结果处理
        //RobotActionProvider.getInstance().etMicAngle(0);//设置6mic拾音方向
		//SpeechPlugin.getInstance().setViewSpeakListener(speakListener);      //设置语音合成（文字转语音）监听; 合成被打断，合成开始，合成结束
		//SpeechPlugin.getInstance().setRecognizeZoon("导航|第二个知识库|第三个知识库|xxx");  //设置语料库优先
        		


		====================================================================
		
		@Override
	    protected void onStart () {
	        super.onStart();
	        SpeechPlugin.getInstance().startRecognize();    //打开录音开关开始识别
			RobotActionProvider.getInstance().setBeam(0);  //设置8mic识别语音的方向
			//RobotActionProvider.getInstance().etMicAngle(0);//设置6mic拾音方向
	    }

	    @Override
	    protected void onStop () {
	        super.onStop();
	        SpeechPlugin.getInstance().stopRecognize();	//关闭录音
	        SpeechPlugin.getInstance().stopSpeak();
	    }


		====================================================================
		/**
		 * Created by ye on 2017/11/10.
		 * 语音识别监听处理
		 */
		
		public class SpeechRecoProcess implements IRecognizeListener {
		    private static final String TAG = SpeechRecoProcess.class.getSimpleName();
		
		    @Override
		    public void onBeginOfSpeech () {
		
		    }
		
		    @Override
		    public void onError (SpeechError speechError) {
		        int code = speechError.getErrorCode();
		        Log.v(TAG, "onError: " + code);
		        Log.v(TAG, "error cause: " + speechError.getErrorDescription());
		        if (code == 10114) {
		            SpeechPlugin.getInstance().startSpeak("网络连接异常！");
		        } else if (code == 20001) {
		            SpeechPlugin.getInstance().startSpeak("网络未连接！");
		        }
		    }
		
		    @Override
		    public void onEndOfSpeech () {
		
		    }
		
		    @Override
		    public void onResult (String s) {
		        //语音识别结果返回
		    }
		
		    @Override
		    public void onVolumeChanged (int i, byte[] bytes) {
		        //语音音量大小返回
		        Log.e(TAG, "====onVolumeChanged==:" + i);
		    }
		}		


		=======================================================================
		/**
		 * Created by ye on 2017/11/10.
		 * AI语料结果回调处理
		 */
		
		public class SpeechResultProcess implements IResultProcessor {
		
		    @Override
		    public void onPartialResult (ReemanResult reemanResult) {
		        //语音处理回答结果返回
		        if (reemanResult == null)
		            return;
		        String json = reemanResult.getJson();
		        int type = reemanResult.getType(); //type: 1:锐曼语料结果, 2:讯飞语料结果
		        if (json == null) {
		
		            return;
		        }
				//锐曼语料结果json格式: {"Code":10001,"Data":"answer","Msg":"知识库1","ReturnUrl":"","Pager":null}
				//Code：结果码， Data：后台所设置答案(多个答案随机返回一个)，Msg：来源知识库名称， ReturnUrl，Pager: 目前无意义 
		    }
		}
	

* 4.控制机器人说话(文字转语音并播放)
	>SpeechPlugin.getInstance().startSpeak("需要转换的文字");

* 5.在web端语料管理添加自己的语料
	> http://120.77.35.5:8001/index.php/admin/home/login.html

* 6.主动发送一条消息，得到语料回调
    >SpeechPlugin.getInstance().onReemanTextUnderstand("去充电")

* 6.详细操作参考【锐曼AI接入文档】

## 集成机器本体动作操控模块(前进，后退，组合动作，左右转等等)

* 1.调用相应方法，详情参考【Reeman硬件接入文档】
	> 例如：RobotActionProvider.getInstance().moveFront(param, speed); 前进

* 2.示例请看BaseBigMan项目中ActionManager.java文件
	
## <span id="CALLBACK">集成外设回调模块(导航状态回调、物体识别、身份证识别回调、打印机等)</span>

* 1.初始化连接外设，设置监听，详情参考【Reeman硬件接入文档】
		 
		private ConnectServer mConnectServer;

		public void init() {
	        // 连接外设
	        mConnectServer = ConnectServer.getInstance(ReemanApp.getInstance(), connection);
	        mConnectServer.registerROSListener(new RosProcess());   //设置外设的监听回调(物体识别，人体检测，导航等回调)
    	}

		public void uninit () {
	        if (mConnectServer != null) {
	            mConnectServer.release();
	            mConnectServer = null;
	        }
    	}
		

	    private RscServiceConnectionImpl connection = new RscServiceConnectionImpl() {
	        public void onServiceConnected (int name) {
	            if (mConnectServer == null)
	                return;
	            if (name == ConnectServer.Connect_D3) {
	                // 3D摄像头回调
	                //                mConnectServer.register3DSensorListener(sensorListener);
	            } else if (name == ConnectServer.Connect_Pr_Id) {
	                // 身份证识别回调
					//mConnectServer.registerIDListener(idListener);
	            }
	        }
	
	        public void onServiceDisconnected (int name) {
	            System.out.println("onServiceDisconnected......");
	        }
	    };

		

		-------------------------------------------------------------------------

		/**
		 * 外设监听回调(物体识别，人体检测，导航状态回调，充电信息回调...)
		 */
		
		public class RosProcess extends OnROSListener {
		    private static final String TAG = RosProcess.class.getSimpleName();
		
		    @Override
		    public void onResult (String result) {
		        Log.e(TAG, "----OnROSListener.onResult()---result:" + result);
		        if (result != null) {
		            if (result.startsWith("od:")) {
		                //Log.e(TAG, "收到物体识别回调：  " + result);
		            } else if (result.startsWith("laser:[")) {
						//激光测距结果，结果格式: laser:[2.1] 单位m 	
					} else if (result.startsWith("pt:[")) {
		                //人体检测结果回调，注：机器有3D摄像头时才会有数据返回
		            } else if (result.startsWith("move_status:")) {
		                Log.e(TAG, "收到导航信息回调：  " + result);
		            } else if (result.equals("bat:reached")) {
		                //Log.e(TAG, "收到充电信息回调：  " + result);
		                // SpeechPlugin.getInstance().startSpeak("到达充电区域，开始连接充电桩");
		            } else if (result.equals("sys:uwb:0")) {
		                //Log.e(TAG, "收到导航信息回调：  uwb错误：:" + result);
		            }
		        }
		    }
		}


## 集成检测人体模块(检测到人进行打招呼等)

### 方案一：机器带3D摄像头的情况下
* 1.参考集成回调模块找到人体检测回调
* 2.结果格式为:  pt:[x,y,z]
	> x,y是人体相对机器人之间坐标，z是人体到camera中心点的距离单位为m
* 3.对结果进行相应处理
* 4.注意：此回调每秒回调两次

### 方案二：机器不带3D摄像头的情况下
* 1.集成回调模块找到激光测距回调
* 2.结果模式为: laser:[2.10]  单位m
* 3.此测距并不能分别前方是人还是障碍物，所以需要结合人脸识别


## 集成导航模块
* 1.(导航关键代码)输入坐标点进行导航，注意坐标格式,正常导航为:goal:nav开头，自动充电为：goal:charge开头。三个值分别为：x值，y值，角度;通过扫地图软件获取这三个值
	
		RobotActionProvider.getInstance().sendRosCom("goal:nav[0.05,0.1,-28.0]");
	
* 2.语音控制进行导航(我要找xxx,带我去前台...)(可参考BaseBigMan项目中speech/ReemanSpeech.java)
	
	* 1.通过地图获取地点的坐标并编辑相应坐标点: sdcard\reeman\data下的
locations.cfg文件。(扫描地图时设定)
			
			//根据自己需求地图替换添加相应坐标点
			前台:-13.0,-4.65,-170.0;充电站:-14.5,1.2,2.0;会议室:-4.05,0.9,-93.0;	
	
	
	
	* 2.后台设置导航语料，参考导航示例语料，获取相应导航地点名称
		
			问：取消充电  答：cancel_charge
			问：回前台    答：navigation_前台
			问：去充电    答：charge_充电站
			问：%去会议室 答：navigation_会议室

	* 3.建议初始化时设置语料优先级时将导航知识库的优先级调成第一个
			
			SpeechPlugin.getInstance().setRecognizeZoon("导航|第二个知识库|第三个知识库|xxx");

	* 4.通过名字获取对应坐标(从locations.cfg文件中获取)
		
			String point = SpeechPlugin.getInstance().getContactLocations().get("前台");
	
	* 5.将坐标拼接成导航所能识别的格式,然后进行导航
		
			RobotActionProvider.getInstance().sendRosCom("goal:nav[0.05,0.1,-28.0]");

* 3.取消导航
	
		RobotActionProvider.getInstance().sendRosCom("cancel_goal");

* 4.对导航状态进行相应处理
	
	> `集成外设回调模块`中回调以 "move_status:" 开头

	> 0 : 静止待命   1 : 上次目标失败，等待新的导航命令   2 : 上次目标完成，等待新的导航命令  3 : 移动中，正在前往目的地   4 : 前方障碍物   5 : 目的地被遮挡 6：用户取消导航 7：收到新的导航



## 集成唤醒模块
>添加唤醒模块，对小曼说小曼你好时会自动转动角度让其面对唤醒方向

* 1.添加监听广播
		
		filter.addAction("REEMAN_BROADCAST_WAKEUP");

* 2.获取角度
		
		RobotActionProvider.getInstance().setBeam(0); //设置拾音方向
        SpeechPlugin.getInstance().startRecognize();  //打开识别，确保一直处于识别状态
        int angle = intent.getIntExtra("REEMAN_8MIC_WAY", 0);   //唤醒角度
		wakeUp(angle);	//唤醒处理，wakeUp方法具体参考BaseBigMan项目中NerveManager.java的wakeup方法

		
## 集成自动充电模块

* 1.监听电量相关广播
	
		filter.addAction("ACTION_POWER_CONNECTE_REEMAN");	//电源连接广播(适配器充电，充电桩充电)
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);	//电量变化
        filter.addAction("AUTOCHARGE_ERROR_DOCKNOTFOUND");	//没有找到充电桩
        filter.addAction("AUTOCHARGE_ERROR_DOCKINGFAILURE"); //连接充电桩失败

* 2.确保地图中有充电站坐标

* 3.接收广播做相应处理(可参考BaseBigMan项目中ChargeManager.java)


## 集成打印机模块

* 1.详情可参考【Reeman硬件接入文档】
* 2.初始化外设回调获得 ConnectServer 对象(见集成外设回调模块)
* 3.调用print方法进行打印(代码见BaseBigMan项目中NerveManger.java文件)

		/**
	     * 打印
	     *
	     * @param s               打印机文本，不可为空
	     * @param type            打印机指令，0， 打印模板内容；1，打印自定义模板内容；2，上传bmp图片 (说明)
	     * @param onPrintListener 打印机回调，返回打印机状态码，状态码见文档说明
	     */
	    public void print (String s, int type, OnPrintListener onPrintListener) {
	        if (mConnectServer != null) {
	            Log.v("ReemanSdk", "客户端申请打印");
	            mConnectServer.onPrint(
	                    "{\"data\":[{\"iNums\":\"1\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"【XXXXX支行】\"," +
	                            "\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"欢迎您光临\",\"alignmen\":\"1\"," +
	                            "\"changerow\":\"0\"},{\"text\":\"Welcome to CCB\",\"alignmen\":\"1\",\"feedline\":\"3\"," +
	                            "\"changerow\":\"0\"},{\"text\":\"【F888】\",\"alignmen\":\"1\",\"feedline\":\"2\"," +
	                            "\"changerow\":\"0\",\"bold\":\"1\",\"sizetext\":\"1\"}," +
	                            "{\"text\":\"前面有：【XX】人，请稍候：【XX】clients ahead of you\",\"alignmen\":\"0\"," +
	                            "\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"尊敬的：【" + s + "】客户\"," +
	                            "\"alignmen\":\"0\",\"changerow\":\"0\"},{\"text\":\"您将要办理：【XX】\",\"alignmen\":\"0\"," +
	                            "\"changerow\":\"0\"},{\"text\":\"【打印日期，精确到秒】\",\"alignmen\":\"0\",\"feedline\":\"2\"," +
	                            "\"changerow\":\"0\"},{\"text\":\"不向陌生人汇款、转账，谨防上当！\",\"alignmen\":\"0\"," +
	                            "\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"温馨提示：您是我行优质客户，诚邀你办理我行信用卡。\"," +
	                            "\"alignmen\":\"0\",\"feedline\":\"5\",\"changerow\":\"0\",\"cutpaper\":\"0\"}]}",
	                    1, onPrintListener);
	        } else {
	            Log.v("ReemanSdk", "服务未绑定");
	        }
	    }

* 4.打印机自定义模板属性说明
		
		自定义模板，type 必须为 1 ；text 中字段详细如下
		iNums // 位图索引，例如 1
		text // 文本，例如 “8888”
		linespace // 行间距 取值0-127，单位0.125mm，例如 4
		spacechar // 字符间距 取值0-64，单位0.125mm
		spacechinese // 中文间距 取值0-64，单位0.125mm
		leftmargin // 左边间距 取值0-576，单位0.125mm ，例如 0
		alignmen // 对齐方式 0 左、1 居中、2 右 ，例如 0
		bold // 加粗 0 不加粗、1 加 ，例如 0
		sizechinese // 文字大小 0 无效 1 有效，例如 1
		sizetext // 文本放大 取值(1-8)
		feedline // 走纸 走纸行数
		italic // 斜体 0 取消斜体；1 设置斜体
		underline // 下划线 0 无 1 一个点下划线 2 两个点下划线 其他无效
		changerow // 换行 0 加换行指令 1 不加换行指令
		cutpaper // 切纸 0 全切、1 半切 ，注意：最后一行打印格式一定要加切纸属性
		qr // 0 默认为打印正常文本，不需要输入， 1 打印文本对应二维码
		
		====================================================================
		完整的json模板提供参考

		{
			"data": [
				{
				"iNums": "1",
				"alignmen": "1",
				"changerow": "0"
				},
				{
				"text": "【XXXXX支行】",
				"alignmen": "1",
				"changerow": "0"
				},
				{
				"text": "欢迎您光临",
				"alignmen": "1",
				"changerow": "0"
				},
				{
				"text": "Welcome to CCB",
				"alignmen": "1",
				"feedline": "3",
				"changerow": "0"
				},
				{
				"text": "【F888】",
				"alignmen": "1",
				"feedline": "2",
				"changerow": "0",
				"bold": "1",
				"sizetext": "1"
				},
				{
				"text": "前面有：【XX】人，请稍候：【XX】clients ahead of you",
				"alignmen": "0",
				"feedline": "2",
				"changerow": "0"
				},
				{
				"text": "尊敬的：【XX】客户",
				"alignmen": "0",
				"changerow": "0"
				},
				{
				"text": "您将要办理：【XX】",
				"alignmen": "0",
				"changerow": "0"
				},
				{
				"text": "【打印日期，精确到秒】",
				"alignmen": "0",
				"feedline": "2",
				"changerow": "0"
				},
				{
				"text": "不向陌生人汇款、转账，谨防上当！",
				"alignmen": "0",
				"feedline": "2",
				"changerow": "0"
				},
				{
				"text": "温馨提示：您是我行优质客户，诚邀你办理我行信用卡。",
				"alignmen": "0",
				"feedline": "2",
				"changerow": "0"
				},
				{
				"qr": "1",
				"text": "aBcd1234",
				"leftmargin": "30",
				"sizetext": "6",
				"feedline": "5",
				"changerow": "0",
				"cutpaper": "0"
				}
			]
		}

* 5.关于打印机的bmp图片打印说明

		1.由于打印位图，必须先将位图上传到打印机内，
		  图片必须为位图（bmp格式）
		  调用相应接口上传，按格式上传，成功后返回 9
		2.如果需要多个位图来回切换，上传的路径中，以”;”分隔，
		  那么相对应的位图 iNums 则分别为 1,2,3... 该值在打印时
		  使用。每次上传会覆盖之前的，也就是 iNums 重新分配为
		  1,2,3....
		3.设置属性 iNums 即可，该值表示打印机的第几个位图，
		  例如 1，其他属性值可以不用设置
		4.上传bmp图片，只需要使用一次。图片会存储在打印机。
		  如果不进行修改，则不需要再次上传。
		详细，请参考提供的示例 json

* 6.打印机错误码

		-2 上传bmp文件失败
		-1 打印机状态未正常获取
		 0 打印机正常
		 1 打印机未连接或未上电
		 2 打印机和调用库不匹配
		 3 打印头打开
		 4 切刀未复位
		 5 打印头过热
		 6 黑标错误
		 7 纸尽
		 8 纸将尽
		 9 上传bmp成功

* 7.关于打印机的二维码打印说明

		打印二维码需要几个必要参数：
		1.主动设置 qr 为1打印二维码，默认情况下打印文本，
		  该值为0，不需要填写
		2.设置二维码的左边距 leftmargin，可以固定二维码位置，
		  取值0-27 单位mm
		3.设置 sizetext ，单位长度，即QR码大小，取值1-8
		4.设置 text ，该文本会被转换为二维码

## 集成身份证识别模块

* 1.集成硬件外接回调模块，给身份证识别设置监听(见集成外设回调模块)
* 2.获取信息做相应处理
		
		    /**
		     * 身份证识别回调
		     */
		    private OnIDListener idListener = new OnIDListener.Stub() {
		        @Override
		        public void onResult (IDCardInfo idCardInfo, byte[] bytes) throws RemoteException {
		            Log.e(TAG,
		                    "name: " + idCardInfo.getName() + ",nation: " + idCardInfo.getNation() + ",birthday: " +
		                            idCardInfo.getBirthday() + ",sex: " + idCardInfo.getSex() + ",address: " + idCardInfo
		                            .getAddress() + ",append: " + idCardInfo.getAppendAddress() + ",fpname: " + idCardInfo
		                            .getFpName() + ",grantdept: " + idCardInfo.getGrantdept() + ",idcardno: " + idCardInfo
		                            .getIdcardno() + ",lifebegin: " + idCardInfo.getUserlifebegin() + "," + "lifeend: " +
		                            idCardInfo.getUserlifeend());
		        }
		    };

## 广播说明

* 1.底座运动完成广播
	
			
		action： REEMAN_LAST_MOVTION
		extra： REEMAN_MOVTION_TYPE 类型 int
		extra值说明：1 头部水平，2头部垂直，16 前，17左，18 右
		255 组合动作结束上报
		255 眼睛灯，耳朵灯上报
* 2.急停按钮状态广播

		action: REEMAN_BROADCAST_SCRAMSTATE
		extra: SCRAM_STATE 类型int
		extra值说明：0 按下，1 打开

* 3.电源插拔广播

		action：ACTION_POWER_CONNECTE_REEMAN
		extra：POWERCHARGE 类型 int
		示例：int powcon = intent.getIntExtra("POWERCHARGE", 0);
		extra值说明：0 拔掉充电 ；1 连接电源适配器；2 连接电源充电桩

* 4.6mic唤醒广播

		action: REEMAN_BROADCAST_WAKEUP
		extra: REEMAN_6MIC_WAY 类型 int
		示例： int angle = intent.getIntExtra("REEMAN_6MIC_WAY", -1);
		extra值说明：代表唤醒角度（顺时针）

* 5.8mic唤醒广播

		action: REEMAN_BROADCAST_WAKEUP
		extra: REEMAN_8MIC_WAY
		示例：int angle = intent.getIntExtra("REEMAN_8MIC_WAY", 0);
		extra值说明：代表机器唤醒角度（逆时针）

## 常见错误

* 1.SpeechPlugin.getInstance() 报空指针问题
	
	> 请确保在Application中进行了语音初始化

* 2.集成语音之后，并没有语音相关回调
	
	> 请检查项目中是否有讯飞的msc.jar包以及相关的lib包 libmsc.so文件


