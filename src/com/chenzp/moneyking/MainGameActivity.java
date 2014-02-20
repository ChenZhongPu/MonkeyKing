package com.chenzp.moneyking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.ease.CCEaseExponentialIn;
import org.cocos2d.actions.ease.CCEaseIn;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCAnimate;
import org.cocos2d.actions.interval.CCFadeIn;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCMoveBy;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.menus.CCMenuItemSprite;
import org.cocos2d.nodes.CCAnimation;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.nodes.CCSpriteSheet;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.utils.CCFormatter;


import android.net.Uri;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainGameActivity extends Activity {

	protected CCGLSurfaceView _glSurfaceView;
	
	CCScene scene;
	
	public static final String SCORETAG = "com.chenzp.moneyking.score";
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
		
		private CCSpriteSheet spriteSheet; // 批处理精灵
		
		private List<CCSprite> visibleBads;   // 可见的妖怪，用于碰撞检测
		
		private boolean gameOver = false;    // 游戏是否结束，规定未开始时为false.
		
		private boolean isOn = false;   // 游戏是否在进行中

		private float time = 0; // 猴子历经的时间，也就是分数
		
		private CCEaseIn fall; // 猴子和云团下降的动作(先慢后快)
		
		private CCMoveBy sharpFall;  // 猴子和云团下降的动作(碰撞后的急速下降）
		
		private long firstTime = 0; // 用以实现两次连续按退出
		/**
		 * 屏幕大小
		 */
		private final CGSize WINSIZE;

		/**
		 * 屏幕中点位置
		 */
		private final CGPoint MPOINT;
		
		/**
		 * 当前Activity
		 */
		private final Context app;
		
		// 用于存储最高分数
		private SharedPreferences scorePreferences;
		
		/**
		 * 点击一次猴子上升的高度
		 */
		public static final int  RISE = 60;
		
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
		
		/**
		 * 静态方法，返回GameLayer的场景(Scene)
		 * 
		 * @return
		 */
		public static CCScene scene() {
			CCScene scene = CCScene.node();
			CCColorLayer layer = new GameLayer(ccColor4B.ccc4(255, 255, 255,
					255));
			scene.addChild(layer);
			return scene;
		}

		protected GameLayer(ccColor4B color) {
			super(color);

			// 设置改Layer的触摸事件有效
			this.setIsTouchEnabled(true);

			// 初始化变量
			WINSIZE = CCDirector.sharedDirector().displaySize();
			MPOINT = CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2);
			visibleBads = new ArrayList<CCSprite>();
			app = CCDirector.sharedDirector().getActivity();
			
			// 两秒内下载屏幕高
			float fallDuration = 2.0f;
			// 下降
			CCMoveBy fallMoveBy = CCMoveBy.action(fallDuration, CGPoint.ccp(0, - WINSIZE.height));
			fall = CCEaseIn.action(fallMoveBy, 2.0f);
			scorePreferences = app.getSharedPreferences(SCORETAG, Context.MODE_PRIVATE);
			sharpFall = CCMoveBy.action(1.0f, CGPoint.ccp(0, -WINSIZE.height));
			
			
			
			//预加载音频
			SoundEngine.sharedEngine().preloadEffect(app, R.raw.click);
			SoundEngine.sharedEngine().preloadEffect(app, R.raw.bang);
			

			Log.d("TEST", "屏幕大小"+WINSIZE.width+" -- "+WINSIZE.height);
			// 游戏背景
			CCSprite bg = CCSprite.sprite("bg.png");
			
			bg.setScale(WINSIZE.height / WELL_Y);
			
			bg.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 + (WINSIZE.height / WELL_Y) * bg.getContentSize().height / 2));
			addChild(bg);
			
			// 游戏地面,占屏幕 1/5高.屏幕不能高于1000.
			ground = CCSprite.sprite("ground.png");
			//Log.d("TEST", "旧的地面" + ground.getContentSize().height);
			   // 针对小屏来调整
			ground.setScale(WINSIZE.height / WELL_Y);
		//	Log.d("TEST", "新的地面" + ground.getContentSize().height);
			ground.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 - (WINSIZE.height / WELL_Y) * ground.getContentSize().height / 2));
			addChild(ground,2);
			
			// 地面移动效果
			CCMoveBy moveBy1 = CCMoveBy.action(0.5f, CGPoint.ccp(-100, 0));
			CCMoveBy moveBy2 = CCMoveBy.action(0, CGPoint.ccp(100 , 0));
			CCRepeatForever repeatForever = CCRepeatForever.action(CCSequence.actions(moveBy1, moveBy2));
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
			cloud.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2 - 70));
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
		    
		    CCMenu screenMenu = CCMenu.menu(aboutItem,shareItem,exitItem);
		    screenMenu.alignItemsHorizontally(100 * WINSIZE.width / WELL_X);
		    screenMenu.setPosition(WINSIZE.width / 2 ,60 * WINSIZE.height / WELL_Y);
		    addChild(screenMenu,3);
		    
		    // 显示时间，也就是分数
		    CCBitmapFontAtlas timeLabel = CCBitmapFontAtlas.bitmapFontAtlas("0.0", "bitmapFontTest.fnt");
		    addChild(timeLabel,5,TIMETAG);
		    timeLabel.setScale(1.5f * WINSIZE.height / WELL_Y);
		    timeLabel.setPosition(CGPoint.ccp(150, WINSIZE.height - 100));
		    
		    // 弹出菜单,开始看不见
		    CCSprite popMenu = CCSprite.sprite("pop_menu.png",true);
		    popMenu.setPosition(CGPoint.ccp(WINSIZE.width / 2, -150));
		    popMenu.setScale(WINSIZE.height / WELL_Y);
		    
		    // 重新开始菜单项
		    CCSprite restartSprite = CCSprite.sprite("start.png", true);
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
		    
		    
		    schedule("createBad", 1.5f); // 产生妖怪的调度
		    
		    schedule("check");  // 检测碰撞的调度
		    
		    
			
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
			
			spriteSheet.addChild(monkey, 1, MONKEYTAG);
			addChild(cloud);
			
			monkey.runAction(fall.copy());
			cloud.runAction(fall.copy());
			monkey.runAction(monkeryForever.copy());
			
		
			
			
			// 产生妖怪的调度
			schedule("createBad", 1.5f);
			
			// 重置游戏状态
			isOn = true;
			gameOver = false;
			
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
				if(visibleBads.size() > 0)
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
		
		
		
	}
	
	


}
