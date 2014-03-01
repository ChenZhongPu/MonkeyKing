package com.chenzp.moneyking;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.ease.CCEaseIn;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCAnimate;
import org.cocos2d.actions.interval.CCBezierTo;
import org.cocos2d.actions.interval.CCFadeIn;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCIntervalAction;
import org.cocos2d.actions.interval.CCMoveBy;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.actions.interval.CCSpawn;
import org.cocos2d.actions.tile.CCShatteredTiles3D;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCMultiplexLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemFont;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.menus.CCMenuItemSprite;
import org.cocos2d.nodes.CCAnimation;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabelAtlas;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.nodes.CCSpriteSheet;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.particlesystem.CCParticleFlower;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.transitions.*;
import org.cocos2d.types.CCBezierConfig;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.types.ccGridSize;
import org.cocos2d.utils.CCFormatter;


import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class MainGameActivity extends Activity {

	protected CCGLSurfaceView _glSurfaceView;
	
	CCScene scene;
	/**
	 * 存储分数的sharedPreference的标识
	 */
	public static final String SCORETAG = "com.chenzp.moneyking.score";
	
	/**
	 * 存储签到时间的sharedPreference的标识
	 */
	public static final String LASTSIGN = "com.chenzp.moneyking.signTime";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		// 设置全屏，不休眠
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// 设置该Activity的View
		_glSurfaceView = new CCGLSurfaceView(this);
		setContentView(_glSurfaceView);
		
		
	
	}


	@Override
	protected void onStart() {

		super.onStart();
		Log.d("TEST", "处理start方法");
		
		CCDirector.sharedDirector().attachInView(_glSurfaceView);

		CCDirector.sharedDirector().setDeviceOrientation(CCDirector.kCCDeviceOrientationLandscapeLeft);
		
		CCDirector.sharedDirector().setAnimationInterval(1.0f / 60.0f);

		scene = GameLayer.scene();
		
		CCDirector.sharedDirector().runWithScene(scene);
	
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.d("TEST", "处理pause方法");
		CCDirector.sharedDirector().pause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Log.d("TEST", "处理resume方法");
		CCDirector.sharedDirector().resume();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		Log.d("TEST", "处理stop方法");
		CCDirector.sharedDirector().end();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		// 屏蔽退出键
		case KeyEvent.KEYCODE_BACK:
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}



	/**
	 * 内部类，表示游戏布景层
	 * @author 中普
	 *
	 */
	public static class GameLayer extends CCColorLayer {

		private CCSprite ground; // 地面精灵
		
		private CCSprite monkey; // 猴子精灵
		
		private CCSprite cloud; // 云精灵

		private CCRepeatForever monkeryForever; // 猴子一直飞的action
		
	    private CCRepeatForever repeatForever; // 地面移动的action
		
		private CCSpriteSheet spriteSheet; // 批处理精灵
		
		private List<CCSprite> visibleBads;   // 可见的妖怪，用于碰撞检测
		
		private boolean gameOver = false;    // 游戏是否结束，规定未开始时为false.
		
		private boolean isOn = false;   // 游戏是否在进行中

		private float time = 0; // 猴子历经的时间，也就是分数
		
		private CCEaseIn fall; // 猴子和云团下降的动作(先慢后快)
		
		private CCMoveBy sharpFall;  // 猴子和云团下降的动作(碰撞后的急速下降）
		
		private long firstTime = 0; // 用以实现两次连续按退出
		
		
		/**
		 * 全局变量，用于layer的索引。
		 */
		private int layIndex;
		
		/**
		 * 多布景层
		 */
		CCMultiplexLayer multiplexLayer;
		
		
		/**
		 * 屏幕大小
		 */
		public final static CGSize WINSIZE = CCDirector.sharedDirector().displaySize();

		/**
		 * 屏幕中点位置
		 */
		private final CGPoint MPOINT;
		
		/**
		 * 当前Activity
		 */
		public final static Context app = CCDirector.sharedDirector().getActivity();;
		
		// 用于存储最高分数
		private SharedPreferences scorePreferences;
		
		/**
		 * 点击一次猴子上升的高度
		 */
		public int RISE;
		
		/**
		 * 作为CCSpriteSheet spriteSheet的Tag标识
		 */
		public static final int SHEETTAG = 1;

		/**
		 * 作为猴子的Tag标识
		 */
		public static final int MONKEYTAG = 2;

		/**
		 * 作为时间标签的Tag标识
		 */
		public static final int TIMETAG = 3;
		
		/**
		 * 作为弹出菜单的Tag标识
		 */
		public static final int POPTAG = 4;
		
		/**
		 * 作为分数标签的Tag标识
		 */
		public static final int SCORE = 5;
		
		/**
		 * 作为最高分的Tag标识
		 */
		public static final int BEST = 6;
		
		
		/**
		 * 作为多布景层里的返回Tag标识
		 */
		
		public static final int BACKTAG = 7;
		
		/**
		 * 作为多布景层里的切换菜单Tag标识
		 */
		public static final int SWITCHTAG = 88;
		
		/**
		 * 该变量用于调整游戏难度
		 */
		private float K = 6.0f;  
		
		/**
		 * 游戏难度上升率，因为从10秒到110秒进行 6 -> 2.
		 */
		public static final float RATE = 0.04f;
		
		/**
		 * 测试适应分辨率<br/>
		 * X_K，Y_K的依据是猴子是：180*145；而测试良好的屏幕为1280*720
		 */
		public static final float X_K = 180.0f / 1280.0f;
		public static final float Y_K = 145.0f / 720.0f;
		
		/**
		 * 测试良好的屏幕为1280*720
		 */
		public static final float WELL_X = 1280.0f;
		public static final float WELL_Y = 720.f;
		
		/**
		 * 标识是否进入很难的的等级。
		 */
		private boolean HARD = false;
		
		
		CCMenuItemSprite bigP;
		CCMenuItemSprite smaP;
		
		CCParticleSystem emitter;
		
		int smallNum = MonkeyUtil.getDataFromShared(LASTSIGN, "SMALLNUM", app);
		int bigNum = MonkeyUtil.getDataFromShared(LASTSIGN, "BIGNUM", app);
		
		/**
		 * 标识是否处于大招状态
		 */
		boolean isInBig = false;
		/**
		 * 静态方法，返回GameLayer的场景(Scene)
		 * 
		 * @return
		 */
		public static CCScene scene() {
			CCScene scene = CCScene.node();
			CCColorLayer layer = new GameLayer(ccColor4B.ccc4(139, 131, 134,
					255));
			scene.addChild(layer);
			return scene;
		}

		protected GameLayer(ccColor4B color) {
			super(color);

			// 设置改Layer的触摸事件有效
			this.setIsTouchEnabled(true);

			// 初始化变量
			
			MPOINT = CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2);
			visibleBads = new ArrayList<CCSprite>();
			
			
			// 两秒内下载屏幕高
			float fallDuration = 2.0f;
			// 下降
			CCMoveBy fallMoveBy = CCMoveBy.action(fallDuration, CGPoint.ccp(0, - WINSIZE.height));
			fall = CCEaseIn.action(fallMoveBy, 2.0f);
			scorePreferences = app.getSharedPreferences(SCORETAG, Context.MODE_PRIVATE);
			sharpFall = CCMoveBy.action(1.0f, CGPoint.ccp(0, -WINSIZE.height));
			RISE = (int) (60 * WINSIZE.height / WELL_Y);
			
			
			//预加载音频
			SoundEngine.sharedEngine().preloadEffect(app, R.raw.click);
			SoundEngine.sharedEngine().preloadEffect(app, R.raw.bang);
			

			Log.d("TEST", "屏幕大小"+WINSIZE.width+" -- "+WINSIZE.height);
			// 游戏背景
		/*	CCSprite bg = CCSprite.sprite("bg.png");
			
			bg.setScale(WINSIZE.height / WELL_Y);
			
			bg.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 + (WINSIZE.height / WELL_Y) * bg.getContentSize().height / 2));
			addChild(bg);*/
			
			// 游戏地面,占屏幕 1/5高.屏幕不能高于1000.
			ground = CCSprite.sprite("ground.png");
			//Log.d("TEST", "旧的地面" + ground.getContentSize().height);
			   // 针对小屏来调整
			ground.setScale(WINSIZE.height / WELL_Y);
		//	Log.d("TEST", "新的地面" + ground.getContentSize().height);
			ground.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 - (WINSIZE.height / WELL_Y) * ground.getContentSize().height / 2));
			addChild(ground,2);
			
			// 地面移动效果
			float length = 50.0f * (WINSIZE.height / WELL_Y);
			CCMoveBy moveBy1 = CCMoveBy.action(0.2f, CGPoint.ccp(-length, 0));
			CCMoveBy moveBy2 = CCMoveBy.action(0, CGPoint.ccp(length , 0));
			repeatForever = CCRepeatForever.action(CCSequence.actions(moveBy1, moveBy2));
			ground.runAction(repeatForever);
			
			// 以上是猴子动画的实现
			// 1, 加载帧缓存,通过加载.plist文件
			CCSpriteFrameCache.sharedSpriteFrameCache().addSpriteFrames(
					"monkey_packer.plist");
			// 2, 初始化批处理精灵，通过加载.png文件 (新版的CCSpriteSheet叫CCSpriteBatchNode)
			spriteSheet = CCSpriteSheet
					.spriteSheet("monkey_packer.png");
			
			//spriteSheet.setScale(WINSIZE.height / WELL_Y);
			// 3. 将该批处理精灵作为布景层的子节点
			addChild(spriteSheet, 1, SHEETTAG);

			monkey = CCSprite.sprite("monkey1.png", true);
			monkey.setScale(WINSIZE.height / WELL_Y);
			monkey.setPosition(MPOINT);
			
			spriteSheet.addChild(monkey, 1, MONKEYTAG);
			
			
			

			// 帧动画
			ArrayList<CCSpriteFrame> frames = new ArrayList<CCSpriteFrame>();
			for (int i = 1; i < 5; i++) {
				String pngName = "monkey" + i + ".png";
				CCSpriteFrame frame = CCSpriteFrameCache
						.sharedSpriteFrameCache().spriteFrameByName(pngName);
				frames.add(frame);
			}

			CCAnimation animation = CCAnimation
					.animation("monkey", 0.1f, frames);
			CCAnimate animate = CCAnimate.action(animation, false);
			monkeryForever = CCRepeatForever.action(animate);

			monkey.runAction(monkeryForever);
			
			
			// 云团
			cloud = CCSprite.sprite("cloud.png", true);
			float gap = 70.0f * (WINSIZE.height / WELL_Y);
			cloud.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2 - gap));
			cloud.setScale(WINSIZE.height / WELL_Y);
			addChild(cloud);
			
			// 初始化屏幕菜单
		    CCSprite aboutSprite = CCSprite.sprite("about.png", true);
		    aboutSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite aboutItem = CCMenuItemSprite.item(aboutSprite, aboutSprite, this, "toAbout");
		    aboutItem.setScale(WINSIZE.height / WELL_Y);
		    
		    CCSprite shareSprite = CCSprite.sprite("share.png", true);
		    shareSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite shareItem = CCMenuItemSprite.item(shareSprite, shareSprite, this, "toShare");
		    shareItem.setScale(WINSIZE.height / WELL_Y);
		    
		    CCSprite exitSprite = CCSprite.sprite("exit.png", true);
		    exitSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite exitItem = CCMenuItemSprite.item(exitSprite, exitSprite, this, "toExit");
		    exitItem.setScale(WINSIZE.height / WELL_Y);
		    
		    // 增加新的屏幕菜单项(工具),计划使用scene的切换
            CCSprite toolSprite = CCSprite.sprite("tool.png",true);
            toolSprite.setScale(WINSIZE.height / WELL_Y);
            CCMenuItemSprite toolItem = CCMenuItemSprite.item(toolSprite, toolSprite, this, "toTool");
            toolItem.setScale(WINSIZE.height / WELL_Y);
            
		    
		    
		    CCMenu screenMenu = CCMenu.menu(aboutItem,shareItem,exitItem,toolItem);
		    screenMenu.alignItemsHorizontally(80 * WINSIZE.width / WELL_X);
		    screenMenu.setPosition(WINSIZE.width / 2 ,60 * WINSIZE.height / WELL_Y);
		    addChild(screenMenu,3);
		    
		    // 显示时间，也就是分数
		    CCBitmapFontAtlas timeLabel = CCBitmapFontAtlas.bitmapFontAtlas("0.0", "bitmapFontTest.fnt");
		    timeLabel.setColor(ccColor3B.ccc3(110, 110, 110));
		    addChild(timeLabel,5,TIMETAG);
		    timeLabel.setScale(1.5f * WINSIZE.height / WELL_Y);
		    
		    timeLabel.setPosition(CGPoint.ccp(150 * WINSIZE.height / WELL_Y, WINSIZE.height - 100 * WINSIZE.height / WELL_Y));
		    
		    // 弹出菜单,开始看不见
		    CCSprite popMenu = CCSprite.sprite("pop_menu.png",true);
		    popMenu.setPosition(CGPoint.ccp(WINSIZE.width / 2, -150));
		    
		    // 重新开始菜单项
		  /*  CCSprite restartSprite = CCSprite.sprite("start.png", true);*/
		   // 注释代码是利用 plist文件加载的，下面是为了测试新的图片颜色
		    CCSprite restartSprite = CCSprite.sprite("start.png",true);
		    restartSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite restartItem = CCMenuItemSprite.item(restartSprite, restartSprite, this, "restart");
		    restartItem.setScale(WINSIZE.height / WELL_Y);
		    
		    CCMenu reStartMenu = CCMenu.menu(restartItem);
		    popMenu.addChild(reStartMenu);
		    // 设置重新开始按钮的位置
		    reStartMenu.setPosition(reStartMenu.getParent().getContentSize().width /2,
		    		reStartMenu.getParent().getContentSize().height /2);
		    
		    // 分数和最高分的标签
		    CCBitmapFontAtlas scoreLabel = CCBitmapFontAtlas.bitmapFontAtlas("0.0", "bitmapFontTest.fnt");
		    popMenu.addChild(scoreLabel,1,SCORE);
		    scoreLabel.setPosition(462, scoreLabel.getParent().getContentSize().height - 94); // 此次将位置写死了。因为图片信息唯一且已知。
		    
		    CCBitmapFontAtlas bestLabel = CCBitmapFontAtlas.bitmapFontAtlas("0.0", "bitmapFontTest.fnt");
		    popMenu.addChild(bestLabel,1,BEST);
		    bestLabel.setPosition(462, bestLabel.getParent().getContentSize().height - 202);
		    
		    addChild(popMenu,1,POPTAG);
		    
		    
		    
		    // 布局两个桃子，用于特殊技能
		    
		    CCSprite colorPeach = CCSprite.sprite("color_peach.png",true);
		    bigP = CCMenuItemSprite.item(colorPeach, colorPeach, this, "bigKongFu");
		    
		    CCSprite colorPeach2 = CCSprite.sprite("color_peach.png",true);
		    smaP = CCMenuItemSprite.item(colorPeach2, colorPeach2, this, "smallKongFu");
		     bigP.setScale(1.8f * WINSIZE.height / WELL_Y);
		     smaP.setScale(1.2f * WINSIZE.height / WELL_Y);
		   showPeach();
		     
		     
		    CCMenu skillMenu = CCMenu.menu(bigP,smaP);
		    skillMenu.alignItemsHorizontally();
		    skillMenu.setPosition(WINSIZE.width - bigP.getContentSize().width - 100, WINSIZE.height - bigP.getContentSize().height);
		    addChild(skillMenu,5);
		    
		    // 测试代码
			 emitter = CCParticleFlower.node(500);
				emitter.setTexture(CCTextureCache.sharedTextureCache().addImage("stars_grayscale.png"));
	    		emitter.setLifeVar(0);
	    		emitter.setLife(5);
	    		emitter.setSpeed(100);
	    		emitter.setSpeedVar(0);
	    		emitter.setEmissionRate(10000);
			 
			    addChild(emitter,5);
			 
	    		emitter.setPosition(WINSIZE.width / 2 , WINSIZE.height / 2);
             /* CCMoveBy emittMoveBy = CCMoveBy.action(2.0f, CGPoint.ccp(200, 0));
              CCMoveBy emittMoveBy2 = emittMoveBy.reverse();
              CCSequence emitteSequence = CCSequence.actions(emittMoveBy, emittMoveBy2);
              emitter.runAction(CCRepeatForever.action(emitteSequence));*/
              
	    		
	    	//	setEmitterPosition();
	    		
	    		emitter.setVisible(false);
		    // 测试结束
		    schedule("createBad", 1.5f); // 产生妖怪的调度
		    
		    schedule("check");  // 检测碰撞的调度
		    
		    
			
		}
	
		// 判断是否需要设置桃子的透明度
		private void showPeach()
		{
			if(bigNum <= 0)
			    {
				   Log.d("TEST", "大桃子《0");
			    	 bigP.setOpacity(50);
			    }
			
			if(smallNum <= 0)
			    {
				     Log.d("TEST", "小桃子<0");
			    	 smaP.setOpacity(50);
			    }
		
		}
		
		//屏幕菜单事件的响应
		
		// 1. 关于菜单
		public void toAbout(Object sender){
			Log.d("TEXT", "关于菜单。。");
			Uri webpage = Uri.parse("https://github.com/ChenZhongPu/MonkeyKing");
			Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
			app.startActivity(webIntent);
			
		}
		// 2. 分享菜单
		public void toShare(Object sender){
			
		      Intent intent=new Intent(Intent.ACTION_SEND);
		      
		      intent.setType("text/plain");
		      intent.putExtra(Intent.EXTRA_SUBJECT, "share");
		      intent.putExtra(Intent.EXTRA_TEXT, "#Monkey King# 我在玩一款好玩的游戏---Monkey King,小伙伴们快下载吧! ");
		      app.startActivity(Intent.createChooser(intent, "SHARE MONKEY KING"));
		      
		}
		
		// 3. 离开菜单
		public void toExit(Object sender){
		
			long secondTime = System.currentTimeMillis();
			
			if(secondTime - firstTime > 2000)
			{
				
				CCBitmapFontAtlas tipAtlas = CCBitmapFontAtlas.bitmapFontAtlas("Click one more to leave","bitmapFontTest.fnt");
				tipAtlas.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 + 20));
				addChild(tipAtlas);
			    CCFadeIn fadeIn = CCFadeIn.action(1.5f);
			    CCFadeOut fadeOut = fadeIn.reverse();
			    tipAtlas.runAction(CCSequence.actions(fadeIn, fadeOut));
				firstTime = secondTime;
			}
			else {
				System.exit(0);
			}
			
		}
		
		// 4. 重新开始菜单
		public void restart(Object sender){
			Log.d("TEST", "重新开始");
			 
			// 重置游戏难度
			K = 6;
			
			// 分数置为0
			time = 0;
			CCBitmapFontAtlas label =
					(CCBitmapFontAtlas) getChildByTag(TIMETAG);
			label.setString("0.00");
			
			// 清除屏幕里的妖怪
			spriteSheet.removeAllChildren(false);
			
			// 弹出菜单回到屏幕之外（看不见）
			CCSprite popSprite =
					(CCSprite) getChildByTag(POPTAG);
			popSprite.setPosition(CGPoint.ccp(WINSIZE.width / 2, -150));
			
			// 把猴子和云团重新放在屏幕中间
			monkey.setPosition(MPOINT);
			cloud.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2 - 70));
			float gap = 70.0f * (WINSIZE.height / WELL_Y);
			cloud.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2 - gap));
			
			spriteSheet.addChild(monkey, 1, MONKEYTAG);
			addChild(cloud);
			
			monkey.runAction(fall.copy());
			cloud.runAction(fall.copy());
			monkey.runAction(monkeryForever.copy());
			
			ground.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 - (WINSIZE.height / WELL_Y) * ground.getContentSize().height / 2));
			ground.runAction(repeatForever);
		
			
			
			// 产生妖怪的调度
			schedule("createBad", 1.5f);
			
			// 重置游戏状态
			isOn = true;
			gameOver = false;
			
		}
		
		// 5. 工具菜单
		public void toTool(Object sender){
			
			Log.d("TEST", "toTool..");
			CCDirector.sharedDirector().replaceScene(CCSlideInBTransition.transition(0.1f, getToolScene()));	
		}
		
		
		
		// 小号技能，杀死屏幕内所有妖怪
		@SuppressWarnings("unchecked")
		public void smallKongFu(Object sender)
		{
			Log.d("TEST", "可见的妖怪 ---->"+ visibleBads.size());
			
			if(!isOn) return;
			
			if(smallNum <= 0)
				return;
			
			smallNum--;
			showPeach();
			
			// 异步更新桃子和金币
			int money = MonkeyUtil.getDataFromShared(LASTSIGN, "MONEY", app);
			Log.d("TEST", "钱"+money);
			new UpdatePeachTask(app).execute(money,bigNum,smallNum);
			
			
			// 消灭所有妖怪
			if(visibleBads != null && visibleBads.size() > 0)
			{
				for(CCNode node : visibleBads)
				{
					node.removeFromParentAndCleanup(true);
				}
			}
			Log.d("TEST", "放了小招");
			
		
			
		}
		
		// 大号技能，自我屏蔽十秒
		@SuppressWarnings("unchecked")
		public void bigKongFu(Object sender){
				 
			if(!isOn) return;
			if(bigNum <= 0) return;
			
			bigNum--;
			showPeach();
			
			// 异步更新数据
			int money = MonkeyUtil.getDataFromShared(LASTSIGN, "MONEY", app);
			Log.d("TEST", "钱"+money);
			new UpdatePeachTask(app).execute(money,bigNum,smallNum);
			
			// 放大招
			
			 emitter.setVisible(true);
			 isInBig = true;
			 this.schedule("delBig", 10); // 十秒后有调度。
			 Log.d("TEST", "放大招");
			
		}
		
		// 来解除大招
		public void delBig(float dt)
		{
			emitter.setVisible(false);
			isInBig = false;
			// 解除该调度
			unschedule("delBig");	
		}
		
		// 产生飞行的妖怪
		public void createBad(float dt){
			// 在游戏未结束（正进行）时
			if(isOn && !gameOver)
			{
				Random random = new Random();
				// 随机产生妖怪的种类
				int badIndex = random.nextInt(6) + 1;
				CCSprite bad = CCSprite.sprite("bad"+badIndex+".png",true);
				
				// 随机产生妖怪的初始位置
				int badMinY =  (int) (bad.getContentSize().height / 2.0f + WINSIZE.height / 5);
				int badMaxY = (int) (WINSIZE.height - bad.getContentSize().height / 2.0f);
				int actualY = random.nextInt(badMaxY - badMinY) + badMinY;
				
				bad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actualY));
				bad.setScale(WINSIZE.height / WELL_Y);
			
				
				// 妖怪的动作
				CCMoveBy moveBy = CCMoveBy.action(2.0f, CGPoint.ccp(-(110+WINSIZE.width), 0));
				CCCallFuncN removeBad = CCCallFuncN.action(this, "removeBad");
				CCSequence badSequence = CCSequence.actions(moveBy, removeBad);
				bad.runAction(badSequence);
				
				
				// 加入到spriteSheet
				spriteSheet.addChild(bad);
	
				// 在不是很难的时候的难度调节
				if(!HARD)
				{
					if(badIndex == 1 || badIndex == 3)
					{
						
						Log.d("TEST", "5,6增加上面的妖怪");
						int keyBadIndex = random.nextInt(6) + 1;
						CCSprite keyBad = CCSprite.sprite("bad"+keyBadIndex+".png", true);
						
						int actualKeyY = random.nextInt(150) + (int)WINSIZE.height - 150;
						keyBad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actualKeyY));
						keyBad.setScale(WINSIZE.height / WELL_Y);
						keyBad.runAction(badSequence.copy());
						spriteSheet.addChild(keyBad);
					}
					
					else if(badIndex == 5){
						Log.d("TEST", "5,6增加下面的妖怪");
						int keyBadIndex = random.nextInt(6) + 1;
						CCSprite keyBad = CCSprite.sprite("bad"+keyBadIndex+".png", true);
						
						int actuaKeyY = random.nextInt(150) + (int) WINSIZE.height / 5;
						keyBad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actuaKeyY));
						keyBad.setScale(WINSIZE.height / WELL_Y);
						keyBad.runAction(badSequence.copy());
						spriteSheet.addChild(keyBad);
					}
				}
				
				if (badIndex == 2) {
					Log.d("TEST", "增加中间的妖怪");
					int keyBadIndex = random.nextInt(6) + 1;
					CCSprite keyBad = CCSprite.sprite("bad"+keyBadIndex+".png", true);
					
					int keyMinY = (int) ((2 * WINSIZE.height) / 5.0);
					int keyMaxY = (int) ((3 * WINSIZE.height) / 5.0);
					int actuaKeyY = random.nextInt(keyMaxY - keyMinY) + keyMinY;
					keyBad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actuaKeyY));
					keyBad.setScale(WINSIZE.height / WELL_Y);
					keyBad.runAction(badSequence.copy());
					spriteSheet.addChild(keyBad);
				}
		
			}
			
		}

		/**
		 * 回调函数，用于清除妖怪
		 * @param sender
		 */
		public void removeBad(Object sender){
			
			Log.d("TEST", "妖怪被移除");
			spriteSheet.removeChild((CCSprite) sender, true);
		}
		
		/**
		 * 回调函数，用于检测碰撞
		 * @param sender
		 */
		public void check(float dt){
			// 在游戏进行中时才检测
			if(isOn)
			{
			//	Log.d("TEST", "ison...");
				time += dt;
				// 更新分数
				String string = CCFormatter.format("%2.3f", time / 10);
				CCBitmapFontAtlas label =
						(CCBitmapFontAtlas) getChildByTag(TIMETAG);
				label.setString(string);
				
				// 根据当前分数来调节游戏难度，即修改k的值
				
				// 大于10秒(1分)时增加游戏难度
		        if (time > 10 && time < 110) {
					
		        	K = 6 - RATE * (time - 10);
		        	Log.d("TEST", K + "新的K");
				}
		        
		        // 如果k<2.5则进入很难的阶段
		        if(HARD == false && K < 2.5f)
		        {
		        	HARD = true;
		        }
				
				// 如果猴子掉到地面（1/5 屏幕高)
				if(monkey.getPosition().y <= WINSIZE.height / 5)
				{
					isOn = false; 
					gameOver = true;
					spriteSheet.removeChild(monkey, false); // 移除猴子
					monkey.stopAllActions();
					removeChild(cloud, false);  // 移除云团
					cloud.stopAllActions();
					ground.stopAllActions();  // 停止地面的运动
					
					// 如果不慎掉下，大招功能也会失效
					isInBig = false;
					emitter.setVisible(false);
					
					// 如果有可见的妖怪
					if(visibleBads.size() > 0)
					{
						// 可见的妖怪的停止运动
						for(CCSprite bad : visibleBads)
						{
							bad.stopAllActions();
						}
					}
					
					unschedule("createBad"); // 解除产生妖怪的调度
					
					// 弹出菜单的显示
					CCSprite popSprite =
							(CCSprite) getChildByTag(POPTAG);
					// 修改分数标签
					CCBitmapFontAtlas scoreAtlas = (CCBitmapFontAtlas) popSprite.getChildByTag(SCORE);
					scoreAtlas.setString(CCFormatter.format("%2.1f", time / 10));
					// 最高分
					float best = scorePreferences.getFloat(SCORETAG, 0.0f);
					
					// 如果最高分小于当前分数，重置最高分
					if(best < (time / 10))
					{
						best = time / 10;
						// 写入 SharedPreferences
						SharedPreferences.Editor editor = scorePreferences.edit();
						editor.putFloat(SCORETAG, best);
						editor.commit();
					}
					
					// 修改最高分标签
					CCBitmapFontAtlas bestAtlas = (CCBitmapFontAtlas) popSprite.getChildByTag(BEST);
					bestAtlas.setString(CCFormatter.format("%2.1f", best));		
					
					popSprite.runAction(CCMoveTo.action(.2f, MPOINT));
					
					return;
				}
				
				// 检测是否和妖怪相碰
				visibleBads.clear();
				 
				List<CCNode> bads = spriteSheet.getChildren();
				
				// 填充visibleBads
				if(bads != null && bads.size() > 0)
				{
					for(CCNode node : bads)
					{
						CCSprite sprite = (CCSprite) node;
						if(sprite.getTag() != MONKEYTAG)
						{
							visibleBads.add(sprite);
						}
					}
				}
				
				// 检测碰撞
				if(!isInBig && visibleBads.size() > 0)
				{
					CGRect monkeyRect = CGRect.make(monkey.getPosition().x - monkey.getContentSize().width / K ,
							monkey.getPosition().y - monkey.getContentSize().height / K,
							(monkey.getContentSize().width / K) * 2,
							(monkey.getContentSize().height / K) * 2);
					for(CCSprite sprite : visibleBads)
					{
						// 发生了碰撞
						if(CGRect.intersects(monkeyRect, sprite.getBoundingBox()))
						{
							SoundEngine.sharedEngine().playEffect(app, R.raw.bang);
							gameOver = true;
							monkey.runAction(sharpFall.copy());
							cloud.runAction(sharpFall.copy());
							
						}
					}
				}
				 
			}
		}
		
		/**
		 * 重写触摸事件
		 */
		@Override
		public boolean ccTouchesBegan(MotionEvent event) {
			
			if(gameOver) return true; // 如果游戏结束了，（猴子死了),则屏幕触摸无效
			
			// 由于isOn初始值为false,通过触摸启动游戏
			if(!isOn) isOn = true; 
			
			// 音效
			SoundEngine.sharedEngine().playEffect(app, R.raw.click);
			
			float monkeyY = monkey.getPosition().y;
			
			// 实际的上升高度，防止超过屏幕
			float acturalRise =( (monkeyY + RISE) > WINSIZE.height - 20)? (WINSIZE.height - 20 - monkeyY):RISE;
			
			// 上升
			CCMoveBy riseMoveBy = CCMoveBy.action(.1f, CGPoint.ccp(0, acturalRise));
			
			// 先上升，后下降
			CCSequence sequence = CCSequence.actions(riseMoveBy, fall.copy());
			
			// 停止猴子和云团的动作
	        monkey.stopAllActions();
	        cloud.stopAllActions();
	        
	        // 点击后，猴子和云团同步先上升后下降
	        monkey.runAction(sequence);
	        cloud.runAction(sequence.copy());
	        
	        // 猴子的飞行动作
	        monkey.runAction(monkeryForever.copy());
	        
			return super.ccTouchesBegan(event);
		}
		
		
		
		
		/**
		 * 获取Tool场景
		 * @return
		 */
		public  CCScene getToolScene()
		{
			multiplexLayer = CCMultiplexLayer.node
					(new ToolLayer(ccColor4B.ccc4(139, 131, 134,
							255)), new HelpLayer(),null);
			CCScene scene = CCScene.node();
			scene.addChild(multiplexLayer);
			
		     // 回到游戏主界面
			CCSprite backSprite = CCSprite.sprite("back.png",true);
			CCMenuItemSprite back = CCMenuItemSprite.item(backSprite,backSprite, this, "toBack");		
			back.setScale(WINSIZE.height / WELL_Y);
			CCMenu menuBack = CCMenu.menu(back);
			// 此处的坐标要修改。。。！！！
			menuBack.setPosition(backSprite.getContentSize().width, WINSIZE.height -  backSprite.getContentSize().width);
			scene.addChild(menuBack,1,BACKTAG);
			
			//  签到等布景层
			
			CCSprite tabSprite = CCSprite.sprite("tab.png",true);
			CCMenuItemSprite one = CCMenuItemSprite.item(tabSprite,tabSprite, this, "toChange");
			one.setScale(1.4f * WINSIZE.height / WELL_Y);
			CCMenuItemSprite two = CCMenuItemSprite.item(tabSprite,tabSprite, this, "toChange");
			two.setScale(1.4f * WINSIZE.height / WELL_Y);
			one.setTag(0);
			two.setTag(1);
			
			CCMenu menu = CCMenu.menu(one,two);
			menu.setPosition((one.getContentSize().width / 2 - 10)* WINSIZE.height / WELL_Y, WINSIZE.height / 2);
			scene.addChild(menu,SWITCHTAG);
		    menu.alignItemsVertically();
		    
			multiplexLayer.switchTo(0);
			layIndex = 0;
			return scene;
		}
		
		public void toChange(Object sender)
		{
			int i = ((CCMenuItemSprite)(sender)).getTag();
			if(i == layIndex) return;
			layIndex = i;
			Log.d("TEXT", "toChange " + i);
			multiplexLayer.switchTo(i);
		}
		
		public void toBack(Object sender) {
			
			CCDirector.sharedDirector().replaceScene(CCSlideInTTransition.transition(0.1f, GameLayer.scene()));
		}
		
	}
	

	/**
	 * 内部类表示点开工具菜单后的布景层
	 * @author 中普
	 *
	 */
	static class ToolLayer extends CCColorLayer{

		private SharedPreferences signTimePreferences;
	    private boolean hasSign;
		public int money;
	    
		public static final int MONEYTAG = 10;
		public static final int COINTAG = 11;
		public static final int SINGTAG = 12;
		public static final int PEACHTAG = 13;
		
		/**
		 * 大桃子数目
		 */
		public static final int BIGTAG = 14;
		/**
		 * 小桃子数目
		 */
		public static final int SMALLTAG = 15;
        /**
         * 菜单项的大桃子		
         */
		public static final int BIGPTAG = 16;
		/**
		 * 菜单项的小桃子
		 */
		public static final int SMAPTAG = 17;
		
		public static boolean isBig;
		
		
		protected ToolLayer(ccColor4B color) {
			super(color);
			
			Log.d("TEST", "toollayer 构造函数");
	       
		/*	CCSpriteFrameCache.sharedSpriteFrameCache().addSpriteFrames(
					"monkey_packer.plist");
			CCSpriteSheet sheet = CCSpriteSheet
					.spriteSheet("monkey_packer.png");
			addChild(sheet);*/
			
			// 初始化签到时间
			signTimePreferences = GameLayer.app.getSharedPreferences(LASTSIGN, Context.MODE_PRIVATE);
			
			// 初始化
			money = signTimePreferences.getInt("MONEY", 0);
		
			// 签到的菜单
			CCMenuItemSprite signItem;
			
			
			if(checkSign())
			{
				Log.d("TEST", "可以签到");
				CCSprite signSprite = CCSprite.sprite("sign1.png",true);
				signItem = CCMenuItemSprite.item(signSprite, signSprite, this, "toSign");
				hasSign = false;
			}
			else {
				CCSprite signSprite = CCSprite.sprite("sign2.png",true);
				signItem =  CCMenuItemSprite.item(signSprite, signSprite, this, "toSign");
				hasSign = true;
			}
			signItem.setScale(GameLayer.WINSIZE.height / GameLayer.WELL_Y);
			
			CCMenu signMenu = CCMenu.menu(signItem);
			signMenu.setPosition(GameLayer.WINSIZE.width / 3,GameLayer.WINSIZE.height - 100 * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    addChild(signMenu, 1, SINGTAG);
		    
		    // 金币余额
		    CCBitmapFontAtlas moneyLabel = CCBitmapFontAtlas.bitmapFontAtlas("$ "+money, "bitmapFontTest.fnt");
		    moneyLabel.setPosition(GameLayer.WINSIZE.width / 2 + GameLayer.WINSIZE.height / 2, GameLayer.WINSIZE.height / 2 + 200);
		    moneyLabel.setScale(1.8f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    moneyLabel.setColor(ccColor3B.ccc3(105, 105, 105));
		    addChild(moneyLabel,1, MONEYTAG);
		    
		    // 以下是桃子的逻辑
		    CCSprite pSprite = CCSprite.sprite("peach.png",true);
		    CCMenuItemSprite itemBig = CCMenuItemSprite.item(pSprite, pSprite, this, "toBuy");
		    CCMenuItemSprite itemSmall = CCMenuItemSprite.item(pSprite, pSprite, this, "toBuy");
		    itemBig.setScale(GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    itemSmall.setScale(0.8f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    itemBig.setTag(BIGPTAG);
		    itemSmall.setTag(SMAPTAG);
		    
		    
		    CCMenu buyMenu = CCMenu.menu(itemBig,itemSmall);
		    buyMenu.alignItemsHorizontally(20 * GameLayer.WINSIZE.width / GameLayer.WELL_X);
		    buyMenu.setPosition(GameLayer.WINSIZE.width / 2,  GameLayer.WINSIZE.height / 2);
		    addChild(buyMenu,1, PEACHTAG);
		    
		    
		    // 显示桃子的个数
		    CCBitmapFontAtlas bigPeach = CCBitmapFontAtlas.bitmapFontAtlas("Big Peach : " + MonkeyUtil.getDataFromShared(LASTSIGN, "BIGNUM", GameLayer.app),
		    		"bitmapFontTest.fnt");
		    CCBitmapFontAtlas smallPeach = CCBitmapFontAtlas.bitmapFontAtlas("Small Peach : " + MonkeyUtil.getDataFromShared(LASTSIGN, "SMALLNUM", GameLayer.app),
		    		"bitmapFontTest.fnt");
		    bigPeach.setPosition(GameLayer.WINSIZE.width / 2 - 200, GameLayer.WINSIZE.height / 2 - 2.2f * bigPeach.getContentSize().height);
		    bigPeach.setScale(1.2f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    smallPeach.setPosition(GameLayer.WINSIZE.width / 2 + 200, GameLayer.WINSIZE.height / 2 - 2.2f * bigPeach.getContentSize().height);
		    smallPeach.setScale(1.2f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    
		    bigPeach.setTag(BIGTAG);
		    smallPeach.setTag(SMALLTAG);
		    
		    bigPeach.setColor(ccColor3B.ccc3(112, 128, 144));
		    smallPeach.setColor(ccColor3B.ccc3(112, 128, 144));
		    
		    addChild(bigPeach);
		    addChild(smallPeach);
		   
		}
		
	/**
	 * 用于检测用户是否可以签到
	 * @return
	 */
		private boolean checkSign()
		{
			Calendar newCalendar = Calendar.getInstance();
			//Log.d("TEST", newCalendar.get(Calendar.YEAR)+" "+newCalendar.get(Calendar.MONTH));
			Calendar oldCalendar = Calendar.getInstance();
			int year = signTimePreferences.getInt("YEAR", 1999);
			Log.d("TEST", "存储的Year "+year);
			int month = signTimePreferences.getInt("MONTH", 1);
			int day = signTimePreferences.getInt("DAY", 1);
			oldCalendar.set(year, month, day);
			return MonkeyUtil.isAfterADay(newCalendar, oldCalendar);
		}
		
		/**
		 * 响应签到事件
		 */
		public void toSign(Object sender)
		{
			if(hasSign) return;
			
			hasSign = true;
			Calendar calendar = Calendar.getInstance();
			CCBitmapFontAtlas moneyAtlas = 
			(CCBitmapFontAtlas)(getChildByTag(MONEYTAG));
			money += 10;
			
			// 多线程将文件写入
			new WriteMoneyTask(signTimePreferences,money).execute(calendar);
			
		
			// 金币的波浪效果
			CCShatteredTiles3D shatter = CCShatteredTiles3D.action(10, true, ccGridSize.ccg(15, 10), 1.5f);
			// 金币贝瑟尔曲线效果
			CCBezierTo[] beziers = new CCBezierTo[5];
			for(int i = 0; i< 5;i++)
			{
				int flag;
				if(i % 2 == 0) flag = 1;
				else {
					flag = -1;
				}
				CCBezierConfig config = new CCBezierConfig();
				config.endPosition = getChildByTag(MONEYTAG).getPosition();
				config.controlPoint_1 = CGPoint.ccp(GameLayer.WINSIZE.width + 60 * i *flag, GameLayer.WINSIZE.height / 2 + 40 * i *flag);
				config.controlPoint_2 = CGPoint.ccp(GameLayer.WINSIZE.width, GameLayer.WINSIZE.height);
				beziers[i] = CCBezierTo.action(1.5f, config);
			}
			
			// 一个回调函数，用于清除金币
			CCCallFuncN delCoin = CCCallFuncN.action(this, "toDelCoin");
			
			
			// 初始化5个金币
			CCSprite[] coins = new CCSprite[5];
			for(int i = 0;i < 5; i++)
			{
				
				coins[i] = CCSprite.sprite("coin1.png",true);
				coins[i].setScale(GameLayer.WINSIZE.height / GameLayer.WELL_Y);
				coins[i].setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2);
				addChild(coins[i],1);
			}
			
			
			for(int i = 0; i < 5; i++)
			{
				// 以上两种运动的组合
				CCSpawn spawn = CCSpawn.actions(shatter.copy(), beziers[i]);
			
				coins[i].runAction(CCSequence.actions(spawn, delCoin));
				
			}
			
			moneyAtlas.setString("$ "+money);
			
		}
		
		
		/**
		 * 回调函数，用于清除金币
		 * @param sender
		 */
		public void toDelCoin(Object sender){
			
			removeChild((CCSprite)sender, true);
			
		}
	
		
		public void toBuy(Object sender){
			Log.d("TEST", "toBuy");
			
			Log.d("TEST", ((CCMenuItemSprite)sender).getTag()+"");
			
			if(((CCMenuItemSprite)sender).getTag() == BIGPTAG)
			{
				if(money < 3) return;
				isBig = true;
			}
			else if(((CCMenuItemSprite)sender).getTag() == SMAPTAG){
				if(money < 1) return;
				isBig = false;
			}
			
			BuyDialog buyDialog =new BuyDialog(ccColor4B.ccc4(41, 41, 41,
					50));
			//this.setOpacity(200);
			
			// 为实现模态对话框的效果，设置各个精灵的透明度,
			// 为更好模拟对话框，将相关菜单禁用
			CCBitmapFontAtlas sprite = (CCBitmapFontAtlas) getChildByTag(MONEYTAG);
            sprite.setOpacity(50);
            
            sprite =
            		(CCBitmapFontAtlas) getChildByTag(BIGTAG);
            sprite.setOpacity(50);
            
            sprite =
            		(CCBitmapFontAtlas) getChildByTag(SMALLTAG);
            sprite.setOpacity(50);
            
            
            CCMenu menu = (CCMenu) getChildByTag(PEACHTAG);
            menu.setOpacity(50);
            menu.setIsTouchEnabled(false);
            
            menu = (CCMenu) getChildByTag(SINGTAG);
            menu.setOpacity(50);
			menu.setIsTouchEnabled(false);
            
            menu = 
            (CCMenu) this.getParent().getParent().getChildByTag(GameLayer.BACKTAG);
            menu.setOpacity(50);
            menu.setIsTouchEnabled(false);
            
         
        
            
            Log.d("TEST", this.getParent().getParent()+"..!!");
			
			buyDialog.setPosition(GameLayer.WINSIZE.width / 5.0f,GameLayer.WINSIZE.height / 6.0f);
			addChild(buyDialog);
			
			
		}
	    

	}

	
	
	
	
	/**
	 * 使用说明层
	 * @author 中普
	 *
	 */
	public static class HelpLayer extends CCLayer{
		
		protected HelpLayer() {
			
			CCBitmapFontAtlas loveAtlas = 
			  CCBitmapFontAtlas.bitmapFontAtlas("They are both convinced", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2 + loveAtlas.getContentSize().height);
			addChild(loveAtlas);
			
			loveAtlas =
			  CCBitmapFontAtlas.bitmapFontAtlas("that a sudden passion joined them.", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2);
			addChild(loveAtlas);
			
			loveAtlas =
					CCBitmapFontAtlas.bitmapFontAtlas("Such certainty is beautiful,", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2 - loveAtlas.getContentSize().height);
			addChild(loveAtlas);
			
			
			loveAtlas =
					CCBitmapFontAtlas.bitmapFontAtlas("But uncertainty is more beautiful still.", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2 - 2 * loveAtlas.getContentSize().height);
			addChild(loveAtlas);
			
			loveAtlas =
					CCBitmapFontAtlas.bitmapFontAtlas("From -- Love at first sight", "bitmapFontTest.fnt");
			
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2 + loveAtlas.getContentSize().width / 3, GameLayer.WINSIZE.height / 2 - 3 * loveAtlas.getContentSize().height);
			addChild(loveAtlas);
		}
	}
    
}
