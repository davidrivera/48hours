package com.protodx.hours;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.math.MathUtils;

import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 22:43:20 - 15.07.2010
 */
public class TestActivity extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int RACETRACK_WIDTH = 64;

	private static final int OBSTACLE_SIZE = 16;
	private static final int CAR_SIZE = 16;

	private static final int CAMERA_WIDTH = RACETRACK_WIDTH * 5;
	private static final int CAMERA_HEIGHT = RACETRACK_WIDTH * 3;

	// ===========================================================
	// Fields
	// ===========================================================

	private Camera mCamera;

	private BitmapTextureAtlas mVehiclesTexture;
	private TiledTextureRegion mVehiclesTextureRegion;

	private BitmapTextureAtlas mBoxTexture;
	private ITextureRegion mBoxTextureRegion;

	private BitmapTextureAtlas mRacetrackTexture;
	private ITextureRegion mRacetrackStraightTextureRegion;
	private ITextureRegion mRacetrackCurveTextureRegion;

	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;

	private Body mCarBody;
	private TiledSprite mCar;
	
	private BitmapTextureAtlas mHouse;
	private ITextureRegion mHouseLine;
	private ITextureRegion mHouseTopRight;
	private ITextureRegion mHouseTopLeft;
	
	private BitmapTextureAtlas mGuy;
	private TiledTextureRegion mGuyReg;
	private AnimatedSprite  mAnimeGuy;
	private static int   SPR_COLUMN  = 4;
	private static int   SPR_ROWS  = 1;
	
	private BitmapTextureAtlas mDpad;
	private ITextureRegion mDpadArrow;
	private ITextureRegion mAbutton;
	private ITextureRegion mBbutton;
	
	private boolean mInMotion = false;
	
	private BitmapTextureAtlas mWeapons;
	private ITextureRegion mStar;
	private Sprite mStarSprite;
	private Body mStarBody;
	
	private ArrayList<PhysicsConnector> mPhysicsConnectors;
	private ArrayList<Sprite> mNinjaStars;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		this.mPhysicsConnectors = new ArrayList<PhysicsConnector>();
		this.mNinjaStars = new ArrayList<Sprite>();

		this.mVehiclesTexture = new BitmapTextureAtlas(this.getTextureManager(), 128, 16, TextureOptions.BILINEAR);
		this.mVehiclesTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mVehiclesTexture, this, "vehicles.png", 0, 0, 6, 1);
		this.mVehiclesTexture.load();

		this.mRacetrackTexture = new BitmapTextureAtlas(this.getTextureManager(), 128, 256, TextureOptions.REPEATING_NEAREST);
		this.mRacetrackStraightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mRacetrackTexture, this, "racetrack_straight.png", 0, 0);
		this.mRacetrackCurveTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mRacetrackTexture, this, "racetrack_curve.png", 0, 128);
		this.mRacetrackTexture.load();
		
		
			
		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 96, 64, TextureOptions.BILINEAR);
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base_0.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob_0.png", 64, 0);
		this.mOnScreenControlTexture.load();
		
		this.mDpad = new BitmapTextureAtlas(this.getTextureManager(),150,46, TextureOptions.BILINEAR);
		this.mDpadArrow = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mDpad, this, "arrow.png",0,0);
		this.mAbutton = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mDpad, this, "a.png",50,0);
		this.mBbutton = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mDpad, this, "b.png",100,0);
		this.mDpad.load();

		this.mBoxTexture = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.mBoxTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBoxTexture, this, "box.png", 0, 0);
		this.mBoxTexture.load();
		
		this.mHouse = new BitmapTextureAtlas(this.getTextureManager(),625,288,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mHouseLine = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHouse,this,"line.png",288,0);
		this.mHouseTopRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHouse,this,"house_right.png",0,0);
		this.mHouseTopLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHouse,this,"house_left.png",337,0);
		this.mHouse.load();
		
		this.mGuy = new BitmapTextureAtlas(this.getTextureManager(), 98, 30, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mGuyReg = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mGuy, this,"ninja2.png", 0, 0, SPR_COLUMN, SPR_ROWS);
		mGuy.load();
		
		this.mWeapons = new BitmapTextureAtlas(this.getTextureManager(),24,24,TextureOptions.BILINEAR);
		this.mStar = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mWeapons, this, "star.png",0,0);
		this.mWeapons.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
//		this.mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, 0), false, 8, 1);
		
		this.initRacetrack();
		this.initRacetrackBorders();
		this.initCar();
		//this.initObstacles();
		//this.createGameControls();
		this.initOnScreenControls();

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		this.mPhysicsWorld.setContactListener(createContactListener());
		return this.mScene;
	}

	@Override
	public void onGameCreated() {

	}

	// ===========================================================
	// Methods
	// ===========================================================
	
	private void createGameControls(){
		HUD yourHud = new HUD();
	    
		
	    final Sprite left = new Sprite(0, CAMERA_HEIGHT-this.mDpadArrow.getHeight(), this.mDpadArrow.getWidth(), this.mDpadArrow.getHeight(),this.mDpadArrow,this.getVertexBufferObjectManager())
	    {
	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
	        {
	            if (touchEvent.isActionDown())
	            {
	                // move player left
	            	mAnimeGuy.animate(100);
	            	final Body carBody = TestActivity.this.mCarBody; 
					//TestActivity.this.mAnimeGuy.animate(100);
	            	Log.d("onAreaTouch",String.valueOf(Y));
					final Vector2 velocity = Vector2Pool.obtain(-10, carBody.getLinearVelocity().y);
					carBody.setLinearVelocity(velocity);
					
					Vector2Pool.recycle(velocity);
	            }else if(touchEvent.isActionUp()){
	            	mAnimeGuy.stopAnimation();
	            }
	            return true;
	        };
	    };
	    final Sprite right = new Sprite(this.mDpadArrow.getWidth(), CAMERA_HEIGHT-this.mDpadArrow.getHeight(), this.mDpadArrow.getWidth(), this.mDpadArrow.getHeight(),this.mDpadArrow,this.getVertexBufferObjectManager())
	    {
	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
	        {
	            if (touchEvent.isActionDown()){
	                // move player left
	            	mAnimeGuy.animate(100);
	            	final Body carBody = TestActivity.this.mCarBody; 
					//TestActivity.this.mAnimeGuy.animate(100);
	            	Log.d("onAreaTouch",String.valueOf(Y));
					final Vector2 velocity = Vector2Pool.obtain(10, carBody.getLinearVelocity().y);
					carBody.setLinearVelocity(velocity);
					
					Vector2Pool.recycle(velocity);
	            	
	            }else if(touchEvent.isActionUp()){
	            	mAnimeGuy.stopAnimation();
	            }
	            return true;
	        };
	    };
	    final Sprite a = new Sprite(CAMERA_WIDTH-this.mAbutton.getWidth(),CAMERA_HEIGHT-this.mAbutton.getHeight(),this.mAbutton.getWidth(),this.mAbutton.getHeight(),this.mAbutton,this.getVertexBufferObjectManager());
	    right.setRotation(180);
	    
	    yourHud.registerTouchArea(left);
	    yourHud.registerTouchArea(right);
	    yourHud.registerTouchArea(a);
	    
	    yourHud.attachChild(left);
	    yourHud.attachChild(right);
	    yourHud.attachChild(a);
	    
	    this.mCamera.setHUD(yourHud);
	}
	
	private void jump(Body mB){
		if(!mInMotion){
			mB.setLinearVelocity(new Vector2(mB.getLinearVelocity().x,-10.5f));
			mInMotion = true;
		}
	}
	public void fire(){
	    this.mStarSprite = new Sprite(this.mAnimeGuy.getX() + 15, this.mAnimeGuy.getY() -5, this.mStar,this.getVertexBufferObjectManager());
	    this.mNinjaStars.add(mStarSprite);
	    this.mStarSprite.setScale(.5f);
	    final FixtureDef starFixture = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
	    this.mStarBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, this.mStarSprite, BodyType.DynamicBody, starFixture);
	    this.mStarBody.setUserData("w");
	    final PhysicsConnector phyc = new PhysicsConnector(this.mStarSprite, this.mStarBody, true, true);
	    this.mPhysicsConnectors.add(phyc);
	    this.mPhysicsWorld.registerPhysicsConnector(phyc);
	    final Vector2 speed = Vector2Pool.obtain(50, 0);
	    this.mStarBody.setLinearVelocity(speed);
	    Vector2Pool.recycle(speed);
	    this.mScene.attachChild(this.mStarSprite);
	}
	private void initOnScreenControls() {
		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				if(!mAnimeGuy.isAnimationRunning()){
		            if(pValueX>0){//Derecha
		            	mAnimeGuy.animate(new long[]{200, 200, 200}, 0, 2, false);
		            }else{
		                if(pValueX<0){//Izquierda
		                	mAnimeGuy.animate(new long[]{200, 200, 200}, 0, 2, false);
		                }
		            }
		          //  TestActivity.this.mCamera.setCenter(pValueX, pValueY);
				}
				final Body carBody = TestActivity.this.mCarBody;
				//TestActivity.this.mAnimeGuy.animate(100);
				final Vector2 velocity = Vector2Pool.obtain(pValueX * 5, 0);
				carBody.setLinearVelocity(velocity);
				
				Vector2Pool.recycle(velocity);

				final float rotationInRad = (float)Math.atan2(-pValueX, pValueY);
				carBody.setTransform(carBody.getWorldCenter(), rotationInRad);

				TestActivity.this.mCar.setRotation(MathUtils.radToDeg(rotationInRad));
				
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing. */
				Log.d("jfkdsl","fjdksl");
			}
		});
		analogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		//		analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
		//		analogOnScreenControl.getControlBase().setScale(0.75f);
		//		analogOnScreenControl.getControlKnob().setScale(0.75f);
	
		//analogOnScreenControl.setAllowDiagonal(true);
		analogOnScreenControl.refreshControlKnobPosition();

		HUD yourHud = new HUD();
		final Sprite a = new Sprite(CAMERA_WIDTH-this.mAbutton.getWidth(),CAMERA_HEIGHT-this.mAbutton.getHeight(),this.mAbutton.getWidth(),this.mAbutton.getHeight(),this.mAbutton,this.getVertexBufferObjectManager()){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				//this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
		                int eventaction = pSceneTouchEvent.getAction(); 

		                float X = pSceneTouchEvent.getX();
		                float Y = pSceneTouchEvent.getY();

		                switch (eventaction) {
		                   case TouchEvent.ACTION_DOWN:{
		                	   jump(TestActivity.this.mCarBody);
		                	   fire();
		                	   break;
		                   }
		                   case TouchEvent.ACTION_MOVE: {
		            	        
		            	        break;
		            	        }
		                   case TouchEvent.ACTION_UP:{
		                        break;
		                   }
		                }
				return true;
			}
		};
		yourHud.registerTouchArea(a);
		yourHud.attachChild(a);
		this.mCamera.setHUD(yourHud);
		this.mScene.setChildScene(analogOnScreenControl);
	}

	private void initCar() {
		this.mCar = new TiledSprite(20, 20, CAR_SIZE, CAR_SIZE, this.mVehiclesTextureRegion, this.getVertexBufferObjectManager());
		this.mCar.setCurrentTileIndex(0);

		final FixtureDef carFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 100);
		this.mCarBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, this.mCar, BodyType.DynamicBody, carFixtureDef);

		this.mAnimeGuy = new AnimatedSprite(0, 0, this.mGuyReg, this.getVertexBufferObjectManager());
		 // this.mScene.attachChild(this.mAnimeGuy);
		  
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this.mAnimeGuy, this.mCarBody, true, false));

		//this.mScene.attachChild(this.mCar);
		
		 this.mCarBody.setUserData("Player");
		  //this.mAnimeGuy = new AnimatedSprite(0, 0, this.mGuyReg, this.getVertexBufferObjectManager());
		 this.mCamera.setChaseEntity(mAnimeGuy);
		  this.mScene.attachChild(this.mAnimeGuy);
		 
		  //this.mAnimeGuy.animate(100);
	}

	private void initObstacles() {
		this.addObstacle(CAMERA_WIDTH / 2, RACETRACK_WIDTH / 2);
		this.addObstacle(CAMERA_WIDTH / 2, RACETRACK_WIDTH / 2);
		this.addObstacle(CAMERA_WIDTH / 2, CAMERA_HEIGHT - RACETRACK_WIDTH / 2);
		this.addObstacle(CAMERA_WIDTH / 2, CAMERA_HEIGHT - RACETRACK_WIDTH / 2);
	}

	private void addObstacle(final float pX, final float pY) {
		final Sprite box = new Sprite(pX, pY, OBSTACLE_SIZE, OBSTACLE_SIZE, this.mBoxTextureRegion, this.getVertexBufferObjectManager());

		final FixtureDef boxFixtureDef = PhysicsFactory.createFixtureDef(0.1f, 0.5f, 0.5f);
		final Body boxBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, box, BodyType.DynamicBody, boxFixtureDef);
		boxBody.setLinearDamping(10);
		boxBody.setAngularDamping(10);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(box, boxBody, true, true));

		this.mScene.attachChild(box);
	}

	private void initRacetrack() {
		final ITextureRegion houseTextureRegion = this.mHouseLine;
		final ITextureRegion houseTopTextureRegion = this.mHouseTopRight;
		final ITextureRegion houseTopLeft = this.mHouseTopLeft;
		/* Straights. */
		{
			final ITextureRegion racetrackHorizontalStraightTextureRegion = this.mRacetrackStraightTextureRegion.deepCopy();
			racetrackHorizontalStraightTextureRegion.setTextureWidth(3 * this.mRacetrackStraightTextureRegion.getWidth());

			final ITextureRegion racetrackVerticalStraightTextureRegion = this.mRacetrackStraightTextureRegion;

			/* Top Straight */
			//this.mScene.attachChild(new Sprite(RACETRACK_WIDTH, 0, 3 * RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackHorizontalStraightTextureRegion, this.getVertexBufferObjectManager()));
			/* Bottom Straight */
			//this.mScene.attachChild(new Sprite(RACETRACK_WIDTH, CAMERA_HEIGHT - RACETRACK_WIDTH, 3 * RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackHorizontalStraightTextureRegion, this.getVertexBufferObjectManager()));

			/* Left Straight */
			final Sprite leftVerticalStraight = new Sprite(0, RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackVerticalStraightTextureRegion, this.getVertexBufferObjectManager());
			leftVerticalStraight.setRotation(90);
			//this.mScene.attachChild(leftVerticalStraight);
			/* Right Straight */
			final Sprite rightVerticalStraight = new Sprite(CAMERA_WIDTH - RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackVerticalStraightTextureRegion, this.getVertexBufferObjectManager());
			rightVerticalStraight.setRotation(90);
			//this.mScene.attachChild(rightVerticalStraight);
			
			final Sprite right = new Sprite(CAMERA_WIDTH-38,50,15,288,houseTextureRegion, this.getVertexBufferObjectManager());
			this.mScene.attachChild(right);
			final Sprite left = new Sprite(38,50,15,288,houseTextureRegion, this.getVertexBufferObjectManager());
			this.mScene.attachChild(left);
		}

		/* Edges */
		{
			final ITextureRegion racetrackCurveTextureRegion = this.mRacetrackCurveTextureRegion;

			/* Upper Left */
			final Sprite upperLeftCurve = new Sprite(39, 0, RACETRACK_WIDTH, RACETRACK_WIDTH, houseTopLeft, this.getVertexBufferObjectManager());
			upperLeftCurve.setRotation(0);
			this.mScene.attachChild(upperLeftCurve);

			/* Upper Right */
			final Sprite upperRightCurve = new Sprite(CAMERA_WIDTH - 90, 0, RACETRACK_WIDTH, RACETRACK_WIDTH, houseTopTextureRegion, this.getVertexBufferObjectManager());
			upperRightCurve.setRotation(0);
			this.mScene.attachChild(upperRightCurve);

			/* Lower Right */
			final Sprite lowerRightCurve = new Sprite(CAMERA_WIDTH - RACETRACK_WIDTH, CAMERA_HEIGHT - RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackCurveTextureRegion, this.getVertexBufferObjectManager());
			lowerRightCurve.setRotation(270);
			//this.mScene.attachChild(lowerRightCurve);

			/* Lower Left */
			final Sprite lowerLeftCurve = new Sprite(0, CAMERA_HEIGHT - 69, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackCurveTextureRegion, this.getVertexBufferObjectManager());
			//this.mScene.attachChild(lowerLeftCurve);
		}
	}


	private void initRacetrackBorders() {
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

		// Xoffset, yoffset
		final Rectangle bottomOuter = new Rectangle(0, CAMERA_HEIGHT - 46 -2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle topOuter = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle leftOuter = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle rightOuter = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final Rectangle bottomInner = new Rectangle(CAMERA_WIDTH-30, CAMERA_HEIGHT - 2 - RACETRACK_WIDTH, 30, 2, vertexBufferObjectManager);
		final Rectangle topInner = new Rectangle(CAMERA_WIDTH-30, RACETRACK_WIDTH, 30, 2, vertexBufferObjectManager);
		final Rectangle leftInner = new Rectangle(CAMERA_WIDTH-30, RACETRACK_WIDTH, 2, CAMERA_HEIGHT - 2 * RACETRACK_WIDTH, vertexBufferObjectManager);
		final Rectangle rightInner = new Rectangle(CAMERA_WIDTH - 2 - RACETRACK_WIDTH, RACETRACK_WIDTH, 2, CAMERA_HEIGHT - 2 * RACETRACK_WIDTH, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, bottomOuter, BodyType.StaticBody, wallFixtureDef).setUserData("Ground");
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, topOuter, BodyType.StaticBody, wallFixtureDef).setUserData("Top");
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, leftOuter, BodyType.StaticBody, wallFixtureDef).setUserData("wall");
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, rightOuter, BodyType.StaticBody, wallFixtureDef).setUserData("wall");

		//PhysicsFactory.createBoxBody(this.mPhysicsWorld, bottomInner, BodyType.StaticBody, wallFixtureDef).setUserData("wall");
		//PhysicsFactory.createBoxBody(this.mPhysicsWorld, topInner, BodyType.StaticBody, wallFixtureDef).setUserData("wall");
		//PhysicsFactory.createBoxBody(this.mPhysicsWorld, leftInner, BodyType.StaticBody, wallFixtureDef).setUserData("wall");
		//PhysicsFactory.createBoxBody(this.mPhysicsWorld, rightInner, BodyType.StaticBody, wallFixtureDef).setUserData("wall");

		
		this.mScene.attachChild(bottomOuter);
		this.mScene.attachChild(topOuter);
		this.mScene.attachChild(leftOuter);
		this.mScene.attachChild(rightOuter);

		//this.mScene.attachChild(bottomInner);
		//this.mScene.attachChild(topInner);
		//this.mScene.attachChild(leftInner);
		//this.mScene.attachChild(rightInner);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private ContactListener createContactListener(){
		ContactListener mContactListener = new ContactListener(){

			@Override
			public void beginContact(Contact arg0) {
				final Fixture x = arg0.getFixtureA();
				final Fixture y = arg0.getFixtureB();
				if(x != null && y != null){
					if(x.getBody().getUserData().equals("Player") && (y.getBody().getUserData().equals("Ground") || y.getBody().getUserData().equals("wall") )){
						TestActivity.this.mInMotion = false;
					}else if((x.getBody().getUserData().equals("Ground")  || y.getBody().getUserData().equals("wall") ) && y.getBody().getUserData().equals("Player")){
						TestActivity.this.mInMotion = false;
					}
					
					if(x.getBody().getUserData().equals("wep")) {
						Body bodyToRemove = x.getBody();
						for(PhysicsConnector connector : TestActivity.this.mPhysicsConnectors){ //mPhysicsConnectors is the list of your coins physics connectors.
							if(connector.getBody() == bodyToRemove)
								removeSpriteAndBody(connector); //This method should also delete the physics connector itself from the connectors list.
						}
					}
					if(y.getBody().getUserData().equals("wep")){
						Body bodyToRemove = y.getBody();
						for(PhysicsConnector connector : TestActivity.this.mPhysicsConnectors){ //mPhysicsConnectors is the list of your coins physics connectors.
							if(connector.getBody() == bodyToRemove)
								removeSpriteAndBody(connector); //This method should also delete the physics connector itself from the connectors list.
						}
					}
				}
				
			}

			@Override
			public void endContact(Contact arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void postSolve(Contact arg0, ContactImpulse arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void preSolve(Contact arg0, Manifold arg1) {
				// TODO Auto-generated method stub
				
			}
			
		};		
		return mContactListener;
		
	}
	private void removeSpriteAndBody(PhysicsConnector connector){
		TestActivity.this.mPhysicsWorld.unregisterPhysicsConnector(connector);
		TestActivity.this.mPhysicsWorld.destroyBody(connector.getBody());
		int thing = TestActivity.this.mPhysicsConnectors.indexOf(connector);
		TestActivity.this.mPhysicsConnectors.remove(thing);
		TestActivity.this.mScene.detachChild(TestActivity.this.mNinjaStars.get(thing));
		TestActivity.this.mNinjaStars.remove(thing);
	}
//	private void destroyBall(final Body ball)
//    {
//        this.runOnUpdateThread(new Runnable(){
//
//            @Override
//            public void run() {
//
//                final Body body = ball.body;
//                mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(ball));
//                mPhysicsWorld.destroyBody(body);
//                mScene.detachChild(ball);
//                ballsList.remove(ball);
//            }});
//
//    }
}
