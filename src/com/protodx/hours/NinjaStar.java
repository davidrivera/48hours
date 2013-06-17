package com.protodx.hours;

import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.vbo.ISpriteVertexBufferObject;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class NinjaStar extends Sprite{
	private Body mNinjaBody;
	private float mVelocityX, mVelocityY;
	private final FixtureDef mNinjaStarFixture = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
	
	public NinjaStar(float pX, float pY, float pWidth, float pHeight,ITextureRegion pTextureRegion,ISpriteVertexBufferObject pSpriteVertexBufferObject, PhysicsWorld pWorld) {
		super(pX, pY, pWidth, pHeight, pTextureRegion, pSpriteVertexBufferObject);
		
	    this.mVelocityX = pX;
	    this.mVelocityY = pY;
	    this.mNinjaBody = PhysicsFactory.createCircleBody(pWorld, this, BodyType.DynamicBody, mNinjaStarFixture);
	    this.mNinjaBody.setUserData(NinjaStar.this);
	    pWorld.registerPhysicsConnector(new PhysicsConnector(this, this.mNinjaBody, true, true));
		// TODO Auto-generated constructor stub
	}

}
