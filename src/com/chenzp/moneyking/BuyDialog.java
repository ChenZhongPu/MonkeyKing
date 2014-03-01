package com.chenzp.moneyking;


import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCBezierTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.menus.CCMenuItemSprite;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.nodes.CCSpriteSheet;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.particlesystem.CCQuadParticleSystem;
import org.cocos2d.types.CCBezierConfig;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.types.ccColor4F;

import com.chenzp.moneyking.MainGameActivity.GameLayer;
import com.chenzp.moneyking.MainGameActivity.ToolLayer;


import android.util.Log;

/**
 * 用户自定义对话框，来实现用户使用金币来买桃子
 * @author 中普
 *
 */
public class BuyDialog extends CCColorLayer{

	//private boolean touchedMenu;

	int remainMoney;
	boolean isBig;
	
	int bigP;
	int smallP;
	
	
	
	protected BuyDialog(ccColor4B color) {
		super(color);
		
		Log.d("TEST", "buydialog构造函数");
		remainMoney = MonkeyUtil.getDataFromShared(MainGameActivity.LASTSIGN,
				"MONEY", GameLayer.app);
		
		bigP = MonkeyUtil.getDataFromShared(MainGameActivity.LASTSIGN,
				"BIGNUM", GameLayer.app);
		
		smallP = MonkeyUtil.getDataFromShared(MainGameActivity.LASTSIGN,
				"SMALLNUM", GameLayer.app);
		
		isBig = ToolLayer.isBig;
		//setIsTouchEnabled(false);
		// 取消菜单项	
		
		CCSprite cancelSprite = CCSprite.sprite("cancel.png",true);
		CCMenuItemSprite cancelItem
		    = CCMenuItemSprite.item(cancelSprite, cancelSprite, this, "toCancel");
		//cancelItem.setPosition(CGPoint.ccp(getWidth(), getHeight()));
		cancelItem.setScale(2.0f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		
		CCMenu menu = CCMenu.menu(cancelItem);
		menu.setPosition(CGPoint.ccp(getWidth(), getHeight()));
		addChild(menu);
		
		
		// 购买菜单项
		CCSprite buySprite = CCSprite.sprite("buy.png", true);
		CCMenuItemSprite buyItem
		  = CCMenuItemSprite.item(buySprite, buySprite, this, "toBuy");
		buyItem.setScale(2.0f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		CCMenu buyMenu = CCMenu.menu(buyItem);
		buyMenu.setPosition(CGPoint.ccp(getWidth() / 2, getHeight() / 2));
		addChild(buyMenu);
		
		
		// 提示标题
		CCBitmapFontAtlas label
		  = CCBitmapFontAtlas.bitmapFontAtlas("Click The Cart To Buy", "bitmapFontTest.fnt");
		//CCLabel label = CCLabel.makeLabel("Click The Cart To Buy", "DroidSans", 46);
		label.setColor(ccColor3B.ccc3(0, 0, 0));
		label.setPosition(getWidth()/2 , getHeight() - label.getContentSize().height / 2);
		addChild(label);
		
		
	}

	// 取消菜单的响应事件
	public void toCancel(Object sender){
		
		Log.d("TEST", "tocancel buy");
		restore();
		this.removeFromParentAndCleanup(false);
	}
	
	// 购买响应
	
	@SuppressWarnings("unchecked")
	public void toBuy(Object sender){
		if(isBig)
		{
			if(remainMoney - 3 >= 0)
			{
				remainMoney -= 3;
				Log.d("Test", "Buy,,,,," + remainMoney);
				bigP++;
				
			}
			else {
				return;
			}
		}
		else {
			if(remainMoney - 1 >= 0)
			{
				remainMoney -=1;
				smallP++;
				
			}
			else {
				return;
			}
		}
	 new UpdatePeachTask(GameLayer.app).execute(remainMoney,bigP,smallP);
	 
	 // 改变显示
	 CCLayer layer = (CCLayer) this.getParent();
	 CCBitmapFontAtlas atlas =
			 (CCBitmapFontAtlas) layer.getChildByTag(ToolLayer.MONEYTAG);
	 atlas.setString("$ "+remainMoney);
	 
	 if(isBig)
	 {
		 atlas =
				 (CCBitmapFontAtlas) layer.getChildByTag(ToolLayer.BIGTAG);
		 atlas.setString("Big Peach :"+bigP);
	 }
	 else {
		 atlas =
				 (CCBitmapFontAtlas) layer.getChildByTag(ToolLayer.SMALLTAG);
		 atlas.setString("SAMLL Peach "+smallP);
	}
	
	 
	 CCSprite bSprite = CCSprite.sprite("color_peach.png",true);
	 // 动画效果
	 CCBezierConfig config = new CCBezierConfig();
	 if(isBig)
	 {
		 config.endPosition = CGPoint.ccp(GameLayer.WINSIZE.width / 2 - 250,
				 GameLayer.WINSIZE.height / 2 - 100);
		 config.controlPoint_1 = CGPoint.ccp(GameLayer.WINSIZE.width / 2 - 250,
				 GameLayer.WINSIZE.height / 2 + 200);
		 config.controlPoint_2 = CGPoint.zero();
	 }
	 else {
		 config.endPosition = CGPoint.ccp(GameLayer.WINSIZE.width / 2 + 250,
				 GameLayer.WINSIZE.height / 2 - 100);
		 config.controlPoint_1 = CGPoint.ccp(GameLayer.WINSIZE.width / 2 + 250,
				 GameLayer.WINSIZE.height / 2 + 200);
		 config.controlPoint_2 = CGPoint.ccp(GameLayer.WINSIZE.width, 0);
	}
	
	bSprite.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2);
	bSprite.setScale(GameLayer.WINSIZE.height / GameLayer.WELL_Y);
	addChild(bSprite);
	
	CCBezierTo bezierTo = CCBezierTo.action(0.5f, config);
	bSprite.runAction(CCSequence.actions(bezierTo, CCCallFuncN.action(this, "delPeach")));
	
		
	}
	@Override
	protected void init(ccColor4B color, float w, float h) {
		
		super.init(color, 3 * GameLayer.WINSIZE.width / 5.0f, 2 * GameLayer.WINSIZE.height / 3.0f);
		
		Log.d("TEST", "init..");
		
		
	}

	/**
	 * 回调方法，用于清除动画的桃子
	 * 
	 */
	public void delPeach(Object sender){
		
		((CCNode)sender).removeFromParentAndCleanup(true);
	}
	/**
	 * 用于把各个菜单等恢复到之前状态
	 */
	public void restore() {
		
		CCLayer layer = (CCLayer) this.getParent();
		
		CCMenu menu = (CCMenu) layer.getChildByTag(ToolLayer.PEACHTAG);
		menu.setOpacity(255);
		menu.setIsTouchEnabled(true);
		
		menu = (CCMenu) layer.getChildByTag(ToolLayer.SINGTAG);
		menu.setOpacity(255);
		menu.setIsTouchEnabled(true);
		
		CCBitmapFontAtlas atlas = (CCBitmapFontAtlas) layer.getChildByTag(ToolLayer.MONEYTAG);
		atlas.setOpacity(255);
		
		atlas =
				(CCBitmapFontAtlas) layer.getChildByTag(ToolLayer.BIGTAG);
		atlas.setOpacity(255);
		
		atlas =
				(CCBitmapFontAtlas) layer.getChildByTag(ToolLayer.SMALLTAG);
		atlas.setOpacity(255);
		
		menu = (CCMenu) layer.getParent().getParent().getChildByTag(GameLayer.BACKTAG);
		menu.setOpacity(255);
		menu.setIsTouchEnabled(true);
	}
	
    @Override
	public void onExit()
	{
		super.onExit();
	}
	@Override
	public void onEnter() {
	
		super.onEnter();
		Log.d("TEST", "enter");
	
	}
	
}
