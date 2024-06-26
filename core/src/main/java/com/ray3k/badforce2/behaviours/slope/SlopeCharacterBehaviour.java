package com.ray3k.badforce2.behaviours.slope;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.ray3k.badforce2.Utils;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour.BoundsData;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Core.shapeDrawer;
import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour.MovementMode.*;
import static com.ray3k.badforce2.behaviours.slope.SlopeValues.*;

public abstract class SlopeCharacterBehaviour extends BehaviourAdapter {
    public enum MovementMode {
        WALKING, SLIDING, FALLING, WALL_CLINGING, LEDGE_GRABBING, SWINGING, CEILING_CLINGING, MAGNETING
    }
    private final static Vector2 temp1 = new Vector2();
    private final static Vector2 temp2 = new Vector2();

    public String debugText = "";
    public float deltaX;
    public float deltaY;
    public float gravityY;

    /**
     * The radius of the circle foot fixture that will contact the ground.
     */
    public final float footRadius;
    /**
     * The x offset of the circle foot fixture that will contact the ground.
     */
    public final float footOffsetX;
    /**
     * The y offset of the circle foot fixture that will contact the ground.
     */
    public final float footOffsetY;
    /**
     * The x offset of the origin of the ray that finds the ground immediately below the foot fixture.
     */
    public final float footRayOffsetX;
    /**
     * The y offset of the origin of the ray that finds the ground immediately below the foot fixture.
     */
    public final float footRayOffsetY;
    /**
     * The height of the character's torso which originates from the center of the foot fixture.
     */
    public final float torsoHeight;

    /**
     * The angle tolerance for walkable slopes. 0 would only allow walking on completely flat surfaces. 90 would allow walking on all slopes.
     */
    public float maxWalkAngle = 50;
    /**
     * The angle tolerance for slidable slopes. 0 would only allow sliding on completely flat surfaces. 90 would allow sliding on all slopes. Must be larger than maxWalkAngle.
     */
    public float maxSlideAngle = 75;
    /**
     * The angle tolerance for ceiling collisions. The angle of the normal necessary to move the character out of the collision is compared to 270. 0 would only allow for head collisions that are directly up
     */
    public float maxCeilingAngle = 85;
    /**
     * The distance of the ray that begins at the bottom of the foot fixture and points downward to detect the ground. This must be sufficiently long enough in order to detect steep slopes.
     */
    public float footRayDistance = 1.25f;
    /**
     * The distance of the ray that begins at the x and y coordinate of the character and points in the opposite direction of the magnetAngle
     * @see SlopeCharacterBehaviour#magnetWallAngle
     */
    public float magnetRayDistance = .9f;
    /**
     * The velocity used to correct the position of the character above the ground when it's floating above a slope. This usually only occurs when going down hill.
     */
    public float slopeStickForce = 3;
    /**
     * The additional time the character is allowed to jump after falling off a ledge
     */
    public float coyoteTime = .2f;
    /**
     * The multiplier applied to deltaY when the character releases the jump input while deltaY is still positive.
     * This affects how floaty a short jump is. Values 0-1.
     */
    public float jumpReleaseDampening = .3f;
    /**
     * How much leading time the character has to press the jump input before landing to initiate a jump.
     */
    public float jumpTriggerDelay = .2f;

    /**
     * If true, the character will stick to the ground and slide on slopes as long as the groundAngle is within maxWalkAngle and maxSlideAngle respectively
     */
    public boolean stickToGround = true;
    /**
     * If true, the character can cling to walls if moveWallClingRight() or moveWallClingLeft() are called while
     * contacting a wall in the air.
     */
    public boolean allowClingToWalls;
    /**
     * If true, the character can climb up and down while clinging onto a wall if moveClimbUp() or moveClimbDown() are
     * called.
     */
    public boolean allowClimbWalls;
    /**
     * If true, the character can perform a wall jump while clinging onto a wall if moveJump() is called.
     */
    public boolean allowWallJump;
    /**
     * If true, the character can perform wall jump while they are next to a wall in the air without needing to cling on.
     */
    public boolean allowWallJumpWithoutCling;
    /**
     * If true, automatically cling to walls if the character is adjacent to one in the air.
     */
    public boolean automaticallyClingToWalls;
    /**
     * If true, the character can grab a ledge if it aligns with the corresponding point on the body and
     * moveWallClingRight() or moveWallClingLeft() are called.
     */
    public boolean allowGrabLedges;
    /**
     * If true, the character can perform a jump while grabbing a ledge if moveJump() is called.
     */
    public boolean allowLedgeJump;
    /**
     * If true, automatically grab a ledge if the character is touching one in the air.
     */
    public boolean automaticallyGrabLedges;
    /**
     * If true, the character can walk up slopes that typically the character would slide down. The character will still
     * slide downward if the appropriate input is not pressed.
     */
    public boolean allowWalkUpSlides;
    /**
     * If true, the character is allowed to cling to the bottoms of BoundsBehaviour that are marked as ceilingClingable. The
     * character can move left and right.
     * @see BoundsBehaviour#ceilingClingable
     */
    public boolean allowClingToCeilings;
    /**
     * If true, the character can attach to any surface of a BoundsBehaviour instance and walk against gravity. It is deactivated
     * when the character jumps away. This functionality is dependent on the terrain being smooth.
     */
    public boolean allowMagnet;
    /**
     * If true, the character is allowed to maintain additional momentum if they are holding the input in that direction.
     */
    public boolean maintainExtraLateralMomentum;
    /**
     * The maximum speed that the character is allowed to walk.
     */
    public float lateralMaxSpeed = 16f;
    /**
     * The maximum acceleration that the character has while walking. The actual acceleration is diminished on a curve
     * as the character approaches lateralMaxSpeed.
     */
    public float lateralAcceleration = 20f;
    /**
     * The maximum deceleration that the character has while walking. This is implemented when the character presses
     * input in the opposite direction of which they are moving. This value is on a curve and actual acceleration may be higher.
     */
    public float lateralDeceleration = 40f;
    /**
     * The minimum deceleration that the character has when they stop walking. This is implemented when there is no left
     * or right input. This is used when the character is close to lateralMaxSpeed.
     */
    public float lateralStopMinDeceleration = 15f;
    /**
     * The maximum deceleration that the character has when they stop walking. This is implemented when there is no left
     * or right input. The actual deceleration is on a curve where maximum deceleration is experienced when closer to 0.
     */
    public float lateralStopDeceleration = 36f;
    /**
     * How close the character has to be to a cliff edge to trigger the eventCliffEdge method.
     * @see SlopeCharacterBehaviour#eventCliffEdge(float, boolean)
     */
    public float walkCliffEdgeDistance = .2f;

    /**
     * The maximum speed that the character has when sliding down a slope.
     */
    public float lateralSlideMaxSpeed = 30f;
    /**
     * The maximum accleration that the character has while sliding. The actual acceleration is diminished on a curve
     * as the character approaches lateralMaxSpeed.
     */
    public float lateralSlideAcceleration = 50f;

    /**
     * The maximum speed for left and right movement that the character is allowed to move in the air.
     */
    public float lateralAirMaxSpeed = 12f;
    /**
     * The maximum acceleration for left and right movement that the character has while in the air. The actual acceleration is diminished on a curve
     * as the character approaches lateralMaxSpeed.
     */
    public float lateralAirAcceleration = 20f;
    /**
     * The maximum deceleration for left and right movement that the character has while in the air. This is implemented when the character presses
     * input in the opposite direction of which they are moving. This value is on a curve and actual acceleration may be higher.
     */
    public float lateralAirDeceleration = 25;
    /**
     * The maximum acceleration for left and right movement that the character has while in the air performing a wall
     * jump. This is only effective for the time while the character is in the air defined by wallJumpDeactivateTime.
     * The actual acceleration is diminished on a curve as the character approaches lateralMaxSpeed.
     */
    public float lateralAirWallJumpingAcceleration = 5;
    /**
     * The minimum deceleration for left and right movement that the character has when they stop moving in the air. This is implemented when there is no left
     * or right input. This is used when the character is close to lateralAirMaxSpeed.
     */
    public float lateralAirStopMinDeceleration = 4;
    /**
     * The maximum deceleration for left and right movement that the character has when they stop moving in the air. This is implemented when there is no left
     * or right input. The actual deceleration is on a curve where maximum deceleration is experienced when closer to 0.
     */
    public float lateralAirStopDeceleration = 5;

    /**
     * The maximum speed that the character will slide down the wall by gravity while wall clinging.
     */
    public float wallSlideMaxSpeed = 15;
    /**
     * How fast the character accelerates downwards while wall clinging.
     */
    public float wallSlideDownAcceleration = 10;
    /**
     * How fast the character slows down while sliding up a wall.
     */
    public float wallSlideUpDeceleration = 30;
    /**
     * The maximum speed that the character can climb walls.
     */
    public float wallClimbMaxSpeed = 8;
    /**
     * How fast movement is accelerated while climbing up or down.
     */
    public float wallClimbAcceleration = 50;
    /**
     * How fast movement is decelerated while clinging to a wall, there is no climb input, and allowClimbWalls is true.
     * The actual deceleration is on a curve where maximum deceleration is experienced when closer to 0.
     * @see SlopeCharacterBehaviour#allowClimbWalls
     * @see SlopeCharacterBehaviour#wallClimbMinDeceleration
     */
    public float wallClimbDeceleration = 50;
    /**
     * The minimum deceleration when climbing a wall. This is implemented when there is no climb input and allowClimbWalls
     * is true. This is implemented when vertical movement is close to wallClimbMaxSpeed.
     * @see SlopeCharacterBehaviour#allowClimbWalls
     * @see SlopeCharacterBehaviour#wallClimbMaxSpeed
     */
    public float wallClimbMinDeceleration = 10;
    /**
     * The length of the ray used to check if the character is connected to a wall on the left or right.
     */
    public float wallRayDistance = .4f;
    /**
     * The length of the ray used to check for the ceiling above the character when clinging to the ceiling.
     */
    public float ceilingRayDistance = .9f;
    /**
     * The vertical offset of the ray used to check if the character is connected to a wall on the left or right. This is
     * measured from the offset of the foot fixture.
     */
    public float wallClimbRayYoffset;
    /**
     * The speed of the jump when the character is climbing up and reaches the top of the wall. This allows for landing on
     * top of the platform even if the climb speed is slow.
     */
    public float wallClimbLedgeJumpSpeed = 800;
    /**
     * The maximum acceleration that the character has while moving horizontally when clinging to a ceiling. The actual
     * acceleration is diminished on a curve as the character approaches ceilingClingLateralMaxSpeed
     * @see SlopeCharacterBehaviour#ceilingClingLateralMaxSpeed
     */
    public float ceilingClingLateralAcceleration = 25;
    /**
     * The maximum deceleration that the character has while moving horizontally when clining to a ceiling. This is
     * implemented when the character presses input in the opposite direction of which they are moving. This value is on
     * a curve and actualacceleration may be higher.
     */
    public float ceilingClingLateralDeceleration = 35;
    /**
     * The maximum speed that the character is allowed to move horizontally while clinging to a ceiling.
     */
    public float ceilingClingLateralMaxSpeed = 8;
    /**
     * The minimum deceleration that the character has when they stop walking. This is implemented when there is no
     * left or right input. This is used when the character is close to ceilingClingLateralMaxSpeed.
     * @see SlopeCharacterBehaviour#ceilingClingLateralMaxSpeed
     */
    public float ceilingClingLateralStopMinDeceleration = 10;
    /**
     * The maximum deceleration that the character has when they stop moving horizontally while clinging to the ceiling
     * This is implemented when there is no left or right input. The actual deceleration is on a curve where maximum
     * deceleration is experienced when closer to 0.
     */
    public float ceilingClingLateralStopDeceleration = 40;
    /**
     * The vertical offset of the ray used to check if the character is touching a ledge on the  left or right. This is
     * measured from the offset of the foot fixture.
     */
    public float ledgeGrabYoffset;
    /**
     * The speed of the jump when the character is grabbing a ledge and presses moveClimbUp().
     * @see SlopeCharacterBehaviour#moveClimbUp()
     */
    public float ledgeGrabJumpSpeed = 9;
    /**
     * The angle of the wall jump if jumping from a wall on the character's left side. This angle is mirrored over the
     * vertical axis if the wall is on the right side.
     */
    public float wallJumpAngle = 60;
    /**
     * The speed of the wall jump.
     */
    public float wallJumpSpeed = 25f;
    /**
     * The angle of the ledge jump if jumping while grabbing a ledge. This angle is mirrored over the
     * vertical axis if the wall is on the right side.
     */
    public float ledgeJumpAngle = 90;
    /**
     * The speed of the wall jump.
     */
    public float ledgeJumpSpeed = 22f;
    /**
     * The length of time when the character's acceleration is penalized from the wall jump. Acceleration returns to
     * normal afterwards.
     * @see SlopeCharacterBehaviour#lateralAirWallJumpingAcceleration
     */
    public float wallJumpDeactivateTime = .5f;
    /**
     * The time period from a jump where the character is not allowed to cling to a wall. This prevents the character
     * from clinging to a wall immediately when pressed up in a corner.
     */
    public float clingToWallThreshold = .3f;
    /**
     * The time period from a jump where the chracter is not allowed to grab a ledge. This prevents the character
     * from clinging to a wall immediately when trying to ledge jump.
     */
    public float grabLedgeThreshold = .3f;
    /**
     * The maximum vertical distance that the character is allowed to be below the ledge in order for it to connect.
     */
    public float ledgeGrabMaxDistance = .1f;
    /**
     * The maximum angle of the upper ground edge that is connected to the ledge point which the character is allowed
     * to grab.
     */
    public float ledgeGrabGroundMaxAngle = 30f;
    /**
     * The signed gravity applied to the character while the character is in the air.
     */
    public float gravity = -75f;
    /**
     * The initial velocity of upwards movement when the character presses the jump input.
     */
    public float jumpSpeed = 25f;
    /**
     * The maximum downward velocity when the character is in the air.
     */
    public float terminalVelocity = 20f;
    /**
     * The number of midair jumps the character is allowed to conduct. Set to 0 (default) for no mid-air jumps. Set to -1 for
     * unlimited midair jumps.
     */
    public int midairJumps;
    /**
     * The initial velocity of upwards movement when the character presses the jump input while in the air.
     */
    public float midairJumpSpeed = 20f;
    /**
     * The delay between multiple midair jumps.
     */
    public float midairJumpDelay = .5f;
    /**
     * Set to true to allow the character to jump while sliding.
     */
    public boolean allowJumpingWhileSliding;

    /**
     * The signed gravity applied to the character while the character is swinging.
     */
    public float swingGravity = -30;
    /**
     * The velocity added to the character in the direction of the chracter's swing to prevent a lame movement
     */
    public float swingImpulse = 5;
    /**
     * The friction applied to the character movement so that he won't swing endlessly like a pendulum
     */
    public float swingFriction = .5f;
    /**
     * The swing is deactivated when the character reaches the apex of the swing. This is determined as when the swing
     * angle changes direction.
     */
    public boolean allowSwingTerminationAtApex;
    /**
     * If set to false, the character's velocity is set to 0 when the swing is initiated. The swingImpulse is added
     * afterwards.
     */
    public boolean swingMaintainVelocity = true;
    /**
     * The x offset of the swing anchor body attached to the character body.
     */
    public float swingCharacterAnchorOffsetX;
    /**
     * The y offset of the swing anchor body attached to the character body.
     */
    public float swingCharacterAnchorOffsetY;
    /**
     * The maximum speed for left and right movement that the character is allowed to move manually during a swing.
     */
    public float lateralSwingMaxSpeed = 20;
    /**
     * The maximum acceleration for left and right movement that the character has while swinging. The actual acceleration is diminished on a curve
     * as the character approaches lateralMaxSpeed.
     */
    public float lateralSwingAcceleration = 25;

    /**
     * Set to true to draw slope debug lines.
     */
    public boolean showDebug;

    /**
     * The main fixture of the body that is used to contact walls and ceilings.
     */
    public Fixture torsoFixture;
    /**
     * The bottom fixture of the body that is used to contact the ground.
     */
    public Fixture footFixture;

    /**
     * True if the character is touching the ground.
     */
    public boolean grounded;
    /**
     * True if the character is in the air.
     */
    public boolean falling;
    /**
     * True if the character just landed from a fall.
     */
    public boolean justLanded;
    /**
     * True if the character initiated a jump.
     */
    public boolean jumping;
    /**
     * True if the character just jumped from a wall. The character's air acceleration is penalized while this is true.
     * @see SlopeCharacterBehaviour#wallJumpDeactivateTime
     * @see SlopeCharacterBehaviour#lateralAirWallJumpingAcceleration
     */
    public boolean wallJumping;
    /**
     * True if the character is swinging from a point.
     */
    public boolean swinging;
    /**
     * True if the character was swinging in the last frame.
     */
    public boolean previousSwinging;
    /**
     * The current angle of the swing measured from the character position to the swinging origin.
     */
    public float swingAngle;
    /**
     * The amount of swing angle that has changed since the last frame.
     */
    public float swingDelta;
    /**
     * True if the ground the character is on can be walked on. 90 is completely flat ground.
     */
    public boolean canWalkOnSlope;
    /**
     * True if the ground can be slid on. It must not be walkable ground. 90 is completely flat ground.
     */
    public boolean canSlideOnSlope;
    /**
     * True if the surface the character is on can be jumped from. See allowJumpingWhileSliding.
     */
    public boolean canJump;
    /**
     * True if the character is in the air and can perform a midair jump.
     */
    public boolean canMidairJump;
    /**
     * True if the character is in the air, touching a wall, and can perform a wall jump.
     */
    public boolean canWallJump;
    /**
     * True if the character is in the air, touching a wall, and can perform a ledge jump
     */
    public boolean canLedgeJump;
    /**
     * True if touching a wall.
     */
    private boolean touchingWall;
    /**
     * True if the character is clinging to a wall.
     */
    private boolean clingingToWall;
    /**
     * True if the character is grabbing a ledge.
     */
    private boolean grabbingLedge;
    /**
     * True if the character is clinging to a ceiling.
     */
    private boolean clingingToCeiling;
    /**
     * True if the character is magnet attached to a surface.
     */
    private boolean magneting;
    /**
     * true if hitting a ceiling.
     */
    private boolean hitHead;
    /**
     * true if the character has reached the apex of their jump and is now falling.
     */
    private boolean hitJumpApex;
    /**
     * The speed that the character moves across the ground. Movement is parallel to the ground slope.
     */
    public float lateralSpeed;
    /**
     * The movement mode of the character: walking, sliding, or falling.
     */
    public MovementMode movementMode;

    /**
     * The angle of the collision normal when contacting the ground. 90 is collision with completely flat ground.
     * ContactAngle may vary compared to groundAngle depending on if the collision was with a vertex.
     */
    private float contactAngle;
    /**
     * The angle of the ground that the character last touched. 90 is completely flat ground.
     */
    private float groundAngle;
    /**
     * The contact angle of the last touched wall.
     */
    private float wallContactAngle;
    /**
     * The fixture angle of the last touched wall.
     */
    private float wallFixtureAngle;
    /**
     * The angle of the last touched ceiling.
     */
    private float ceilingAngle;
    /**
     * The ground fixtures that were touched in this frame.
     */
    private final ObjectSet<Fixture> touchedGroundFixtures = new ObjectSet<>();
    /**
     * The ceiling fixtures that were touched in this frame.
     */
    private final ObjectSet<Fixture> touchedCeilingClingFixtures = new ObjectSet<>();
    /**
     * The ground fixtures that were touched in the last frame.
     */
    private final ObjectSet<Fixture> lastTouchedGroundFixtures = new ObjectSet<>();
    /**
     * Clears the lastTouchedGroundFixtures when there is a new frame and a new ground contact has been made.
     */
    private boolean clearLastTouchedGroundFixtures;
    /**
     * The origin of the swinging joint. This body does not interact with the world and serves solely to facilitate the
     * character swinging.
     */
    private Body swingAnchorOrigin;
    /**
     * The body attached to the character that facilitates swinging. This body does not interact with the world otherwise.
     */
    private Body swingAnchorCharacter;
    /**
     * The joint associated with the character swinging.
     */
    private RevoluteJoint swingJoint;
    /**
     * The world x position where the swing joint originates.
     */
    private float swingTargetX;
    /**
     * The world y position where the swing joint originates.
     */
    private float swingTargetY;
    /**
     * The previous amount of swing angle that has changed since the last frame.
     */
    private float previousSwingDelta;
    /**
     * The current angle of the swing joint. Used in conjunction with previousJointAngle to determine the swingDelta.
     * swingDelta determines if the swing must be terminated when allowSwingTerminationAtApex is activated.
     */
    private float swingJointAngle;
    /**
     * The previous angle of the swing joint.
     * @see SlopeCharacterBehaviour#swingJointAngle
     */
    private float previousSwingJointAngle;

    /**
     * Character called moveLeft() for this frame.
     */
    private boolean inputLeft;
    /**
     * Character called moveRight() for this frame.
     */
    private boolean inputRight;
    /**
     * Character called moveJump() for this frame.
     */
    private boolean inputJump;
    /**
     * Character called moveJump() for this frame when inputJump was false last frame.
     */
    private float inputJumpJustPressed;
    /**
     * Character called moveWallClingLeft() for this frame.
     * @see SlopeCharacterBehaviour#moveWallClingLeft()
     */
    private boolean inputWallClingLeft;
    /**
     * Character called moveWallClingRight() for this frame.
     * @see SlopeCharacterBehaviour#moveWallClingRight()
     */
    private boolean inputWallClingRight;
    /**
     * Character called moveCeilingCling() for this frame.
     * @see SlopeCharacterBehaviour#moveCeilingCling()
     */
    private boolean inputCeilingCling;
    /**
     * Character called moveClimbUp() for this frame.
     * @see SlopeCharacterBehaviour#moveClimbUp()
     */
    private boolean inputWallClimbUp;
    /**
     * Character called moveClimbDown() for this frame.
     * @see SlopeCharacterBehaviour#moveClimbDown()
     */
    private boolean inputWallClimbDown;
    /**
     * Character called movePassThroughFloor() for this frame.
     * @see SlopeCharacterBehaviour#movePassThroughFloor()
     */
    private boolean inputPassThroughFloor;
    /**
     * Character called moveSwing() for this frame.
     * @see SlopeCharacterBehaviour#moveSwing(float, float)
     */
    private boolean inputSwing;
    /**
     * This is the first frame where moveSwing() has just been pressed.
     * @see SlopeCharacterBehaviour#moveSwing(float, float)
     */
    private boolean inputSwingJustPressed;
    /**
     * A list of fixtures that the character should pass through. These are typically fixtures associated with a Bounds
     * entity that has canPassThroughBottom set to true and the character just jumped from underneath, the side, or is
     * inside of one and is escaping it.
     * @see BoundsBehaviour#canPassThroughBottom
     */
    private final Array<Fixture> passThroughFixtures = new Array<>();
    /**
     * Counts down continuously and is reset when the character begins to fall. Used to compare against coyoteTime.
     */
    private float coyoteTimer;
    /**
     * Counts down after the character initiates a wall jump. This tracks how long the character's air acceleration is
     * penalized.
     * @see SlopeCharacterBehaviour#lateralAirWallJumpingAcceleration
     * @see SlopeCharacterBehaviour#wallJumpDeactivateTime
     */
    private float wallJumpTimer;
    /**
     * Counts down after the character initiates a midair jump. This tracks how long until the character can conduct
     * another midair jump.
     */
    private float midairJumpTimer;
    /**
     * Counts the number of midair jumps the character has performed. This value is tested against midairJumps before
     * performing another midair jump.
     * @see SlopeCharacterBehaviour#midairJumps
     */
    public int midairJumpCounter;
    /**
     * A list of fixtures that this character is touching which are attached to a BoundsBehaviour that has kinematic set to true.
     * If this list is larger than 0, the character will match the movement speed of the first BoundsBehaviour in this list. All
     * movement will be relative to this platform unless the character jumps or releases from a wall cling. Releasing
     * from a wall cling in particular will transfer the momentum into falling horizontal movement.
     */
    private final ObjectSet<Fixture> movingPlatformFixtures = new ObjectSet<>();
    /**
     * The fixture just below the character as detected via ray cast.
     * @see SlopeCharacterBehaviour#footRayDistance
     * @see SlopeCharacterBehaviour#footRayOffsetX
     * @see SlopeCharacterBehaviour#footRayOffsetY
     */
    private Fixture rayCastedGroundFixture;
    /**
     * The distance that the character must travel vertically to line up with the ledge.
     */
    private float ledgeGrabYadjustment;
    /**
     * The ceiling fixture that the character is touching.
     */
    private Fixture ceilingClingFixture;
    /**
     * True if the character was clinging to the ceiling last frame.
     */
    private boolean lastClingingToCeiling;
    /**
     * The angle of the fixture that the character is attached to in magnet mode.
     */
    private float magnetWallAngle;
    /**
     * How fast the character accelerates while in magnet mode.
     */
    public float magnetLateralAcceleration = 25;
    /**
     * How fast the character decelerates when turning around while in magnet mode.
     */
    public float magnetLateralDeceleration = 35;
    /**
     * How maximum speed of the character while they are in magnet mode.
     */
    public float magnetLateralMaxSpeed = 8;
    /**
     * The minimum deceleration of the character stops while in magnet mode.
     */
    public float magnetLateralStopMinDeceleration = 10;
    /**
     * How fast the character stops while in magnet mode.
     */
    public float magnetLateralStopDeceleration = 40;
    /**
     * What BoundsBehaviour fixtures are touching the foot fixture while in magnet mode.
     */
    private final ObjectSet<Fixture> touchedMagnetFixtures = new ObjectSet<>();
    /**
     * What BoundsBehaviour fixtures are touching the torso fixture while in magnet mode.
     */
    private final ObjectSet<Fixture> touchedTorsoMagnetFixtures = new ObjectSet<>();
    /**
     * Enables magnet mode when the character is grounded and allowMagnet is true.
     * @see SlopeCharacterBehaviour#grounded
     * @see SlopeCharacterBehaviour#allowMagnet
     */
    private boolean inputMagnet;
    /**
     * The fixture that the character is touching while in magnet mode.
     */
    private Fixture magnetFixture;
    /**
     * The fixture that the character was touching last frame in magnet mode.
     */
    private Fixture lastMagnetFixture;
    /**
     * When the character is in magnet mode, this value indicates the direction the character is moving. -1 is left, 1
     * is right, and 0 is standing still. These values are reversed when the character is hanging from a ceiling. The
     * value is preserved until the input is released so that going through loops is not inconvenient.
     */
    private int magnetGoRight;
    /**
     * true if the character is jumping. This value is set to false only if the torso has completely cleared the bounds
     * fixtures. When cleared, magneting is set to false.
     * @see SlopeCharacterBehaviour#magneting
     */
    private boolean magnetJumping;
    /**
     * The speed that the character jumps from the surface while in magnet mode.
     */
    private float magnetJumpSpeed = 15;
    /**
     * true if the character was magneting last frame.
     */
    private boolean lastMagneting;
    private float magnetNoContactTimer;
    private float magnetNoContactDelay = .1f;

    public SlopeCharacterBehaviour(float footOffsetX, float footOffsetY, float footRadius, float torsoHeight, GameObject gameObject) {
        super(gameObject);

        this.footOffsetX = footOffsetX;
        this.footOffsetY = footOffsetY;
        this.footRadius = footRadius;

        footRayOffsetX = footOffsetX;
        footRayOffsetY = footOffsetY - footRadius;

        this.torsoHeight = torsoHeight;
        this.wallClimbRayYoffset = (torsoHeight + footRadius) / 2;
        this.ledgeGrabYoffset = (torsoHeight + footRadius);
    }

    public Fixture setCollisionCircle(float centerX, float centerY, float radius) {
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius);
        temp1.set(centerX, centerY);
        circleShape.setPosition(temp1);

        var body = getBody(this);
        var fixture = body.createFixture(circleShape, .5f);
        circleShape.dispose();

        return fixture;
    }

    public Fixture setCollisionBox(float offsetX, float offsetY, float width, float height) {
        PolygonShape box = new PolygonShape();
        temp1.set(offsetX + width / 2, offsetY + height / 2);
        box.setAsBox(width / 2, height / 2, temp1, 0);

        var body = getBody(this);
        var fixture = body.createFixture(box, .5f);
        fixture.setFriction(0);
        box.dispose();

        return fixture;
    }

    @Override
    public void awake() {
        footFixture = setCollisionCircle(footOffsetX, footOffsetY, footRadius);
        footFixture.getFilterData().categoryBits = CATEGORY_ENTITY;
        footFixture.getFilterData().maskBits = CATEGORY_BOUNDS;

        torsoFixture = setCollisionBox(-footRadius + footOffsetX, footOffsetY, footRadius * 2, torsoHeight);
        torsoFixture.getFilterData().categoryBits = CATEGORY_ENTITY;
        torsoFixture.getFilterData().maskBits = CATEGORY_BOUNDS + CATEGORY_ENTITY;

        gravityY = gravity;
    }

    @Override
    public void fixedUpdate() {
    }

    @Override
    public void lateUpdate(float delta) {
    }

    @Override
    public void update(float delta) {
        var body = getBody(this);
        checkIfGrounded();

        checkIfOnCliff(delta);

        inputLeft = false;
        inputRight = false;
        var lastInputJump = inputJump;
        inputJump = false;
        inputJumpJustPressed -= delta;
        inputWallClimbDown = false;
        inputWallClimbUp = false;
        inputPassThroughFloor = false;
        inputWallClingLeft = false;
        inputWallClingRight = false;
        inputCeilingCling = false;
        var lastInputSwing = inputSwing;
        inputSwing = false;
        inputSwingJustPressed = false;
        inputMagnet = false;

        coyoteTimer -= delta;
        wallJumpTimer -= delta;
        midairJumpTimer -= delta;
        handleControls();
        if (inputJump && !lastInputJump) inputJumpJustPressed = jumpTriggerDelay;
        if (inputSwing && !lastInputSwing) inputSwingJustPressed = true;
        if (swingAnchorOrigin != null ) {
            swingAngle = pointDirection(swingTargetX, swingTargetY, body.getPosition().x, body.getPosition().y);
            previousSwingJointAngle = swingJointAngle;
            swingJointAngle = swingJoint.getJointAngle();
            if (previousSwingJointAngle == 0) previousSwingJointAngle = swingJointAngle;
            previousSwingDelta = swingDelta;
            swingDelta = previousSwingJointAngle - swingJointAngle;
        } else {
            swingAngle = -1;
            swingDelta = 0;
            previousSwingDelta = 0;
            previousSwingJointAngle = 0;
            swingJointAngle = 0;
        }

        handleMovement(delta);

        deltaY += gravityY * delta;
        body.setLinearVelocity(deltaX, deltaY);

        debugText = "Movement Mode: " + movementMode +
            "\nMoving Platform: " + movingPlatformFixtures.size +
            "\nTouchedGroundFixtures: " + touchedGroundFixtures.size +
            "\nGrounded: " + grounded +
            "\nFalling: " + falling +
            "\nPassthrough count: " + passThroughFixtures.size +
            "\nLateral Speed: " + lateralSpeed +
            "\nGround Angle: " + groundAngle +
            "\ntouchedTorsoMagnetFixtures: " + touchedTorsoMagnetFixtures.size +
            "\nCoyote Timer: " + coyoteTimer +
            "\ndeltaX: " + deltaX +
            "\ndeltaY: " + deltaY +
            "\nfps: " + Gdx.graphics.getFramesPerSecond();

        justLanded = false;
        touchingWall = false;
        hitHead = false;
        clearLastTouchedGroundFixtures = true;
        rayCastedGroundFixture = null;
        ceilingClingFixture = null;
        lastMagnetFixture = magnetFixture;
        magnetFixture = null;
    }

    /**
     * Checks if the character is grounded via physically touching a ground fixture. If not, check via ray cast. This is
     * necessary because of steep slopes and to check if the character is on a cliff edge.
     */
    private void checkIfGrounded() {
        var body = getBody(this);
        grounded = touchedGroundFixtures.size > 0;

        boolean needToCheckForRayCastedGroundFixture = true;
        //If the character is not physically touching the ground, shoot a ray down to determine if he should be.
        if (!grounded && !falling) {
            needToCheckForRayCastedGroundFixture = false;
            getUnBox().getWorld().rayCast((fixture, point, normal, fraction) -> {
                if (fixture.getBody().getUserData() instanceof BoundsBehaviour) {
                    var data = (BoundsData) fixture.getUserData();
                    for (var lastTouchedGroundFixture : lastTouchedGroundFixtures) {
                        //If there are any fixtures between the lastTouchedGroundFixture and this raycasted fixture that
                        // is a wall, do not count this as ground. That wall is a cliff and the character has walked
                        // over the edge.
                        if (data.checkFixturesBetween(lastTouchedGroundFixture, fixture1 -> {
                            var angle = ((BoundsData) fixture1.getUserData()).angle;
                            return isEqual360(angle, 90, maxSlideAngle);
                        })) {
                            contactAngle = normal.angleDeg();
                            groundAngle = ((BoundsData) fixture.getUserData()).angle;
                            grounded = true;
                            rayCastedGroundFixture = fixture;
                            var bounds = (BoundsBehaviour) fixture.getBody().getUserData();
                            if (getBody(bounds).getType() == BodyType.KinematicBody) movingPlatformFixtures.add(fixture);
                            return 0;
                        }
                    }
                }
                return 1;
            }, body.getPosition().x + footRayOffsetX, body.getPosition().y + footRayOffsetY, body.getPosition().x + footRayOffsetX, body.getPosition().y + footRayOffsetY - footRayDistance);

            if (!grounded) {
                falling = true;
                if (canWalkOnSlope || allowJumpingWhileSliding) coyoteTimer = coyoteTime;
                movingPlatformFixtures.clear();
            }
        }
        slopeCheck();

        //if there hasn't already been a check for ground via ray cast, do a ray cast to find the fixture directly below
        if (needToCheckForRayCastedGroundFixture) {
            getUnBox().getWorld().rayCast((fixture, point, normal, fraction) -> {
                if (fixture.getBody().getUserData() instanceof BoundsBehaviour) {
                    var data = (BoundsData) fixture.getUserData();
                    for (var lastTouchedGroundFixture : lastTouchedGroundFixtures) {
                        //If there are any fixtures between the lastTouchedGroundFixture and this raycasted fixture that
                        // is a wall, do not count this as ground. That wall is a cliff and the character has walked
                        // over the edge.
                        if (data.checkFixturesBetween(lastTouchedGroundFixture, fixture1 -> {
                            var angle = ((BoundsData) fixture1.getUserData()).angle;
                            return isEqual360(angle, 90, maxSlideAngle);
                        })) {
                            rayCastedGroundFixture = fixture;
                            return 0;
                        }
                    }
                }
                return 1;
            }, body.getPosition().x + footRayOffsetX, body.getPosition().y + footRayOffsetY, body.getPosition().x + footRayOffsetX, body.getPosition().y + footRayOffsetY - footRayDistance);
        }
    }

    private void checkIfOnCliff(float delta) {
        var body = getBody(this);
        for (var fixture : touchedGroundFixtures) {
            var boundsData = (BoundsData) fixture.getUserData();
            var edge = (EdgeShape) fixture.getShape();
            edge.getVertex1(temp1);
            edge.getVertex2(temp2);

            if (temp2.x < temp1.x) {
                var x = temp1.x;
                var y = temp1.y;
                temp1.x = temp2.x;
                temp1.y = temp2.y;
                temp2.x = x;
                temp2.y = y;
            }
            var closeToLeftPoint = pointDistance(body.getPosition().x, body.getPosition().y, temp1.x, temp1.y) < walkCliffEdgeDistance;
            var closeToRightPoint = pointDistance(body.getPosition().x, body.getPosition().y, temp2.x, temp2.y) < walkCliffEdgeDistance;
            var previousAngle = ((BoundsData) boundsData.previousFixture.getUserData()).angle;
            var nextAngle = ((BoundsData) boundsData.nextFixture.getUserData()).angle;
            if (rayCastedGroundFixture != null) {
                if (closeToLeftPoint && !isEqual360(previousAngle, 90, maxSlideAngle)) {
                    eventCliffEdge(delta, false);
                    break;
                } else if (closeToRightPoint && !isEqual360(nextAngle, 90, maxSlideAngle)) {
                    eventCliffEdge(delta, true);
                    break;
                }
            } else {
                if (body.getPosition().x < temp1.x && !isEqual360(previousAngle, 90, maxSlideAngle)) {
                    eventCliffEdge(delta, false);
                    break;
                } else if (body.getPosition().x > temp2.x && !isEqual360(nextAngle, 90, maxSlideAngle)) {
                    eventCliffEdge(delta, true);
                    break;
                }
            }
        }
    }

    private void slopeCheck() {
        canWalkOnSlope = isEqual360(groundAngle, 90, maxWalkAngle);
        canSlideOnSlope = !canWalkOnSlope && isEqual360(groundAngle, 90, maxSlideAngle);
    }

    /**
     * Holding this input moves the character to the left while in the air or while touching the ground.
     */
    public void moveLeft() {
        inputLeft = true;
    }

    /**
     * Holding this input moves the character to the right while in the air or while touching the ground.
     */
    public void moveRight() {
        inputRight = true;
    }

    /**
     * Pressing this input initiates a jump while on the ground. Holding this input increases the height of the jump.
     */
    public void moveJump() {
        inputJump = true;
    }

    /**
     * This input must be held to grab on to walls or ledges on the left side.
     */
    public void moveWallClingLeft() {
        inputWallClingLeft = true;
    }

    /**
     * This input must be held to grab on to walls or ledges on the right side.
     */
    public void moveWallClingRight() {
        inputWallClingRight = true;
    }

    public void moveCeilingCling() {
        inputCeilingCling = true;
    }

    public void moveMagnet() {
        inputMagnet = true;
    }

    /**
     * Holding this input moves the character up while clinging to a wall.
     */
    public void moveClimbUp() {
        inputWallClimbUp = true;
    }

    /**
     * Holding this input moves the character down while clinging to a wall.
     */
    public void moveClimbDown() {
        inputWallClimbDown = true;
    }

    /**
     * Holding this input allows the character to pass through the top of any BoundsBehaviour that is marked as canPassThroughBottom
     */
    public void movePassThroughFloor() {
        inputPassThroughFloor = true;
    }

    /**
     * Initiates a swing at the provided coordinates.
     * @param x
     * @param y
     */
    public void moveSwing(float x, float y) {
        inputSwing = true;
        swingTargetX = x;
        swingTargetY = y;
    }

    /**
     * Applies a force to the character that will only propel them if they are on the ground. They will not be pushed into the air.
     * @param speed
     * @param direction
     */
    public void applyGroundForce(float speed, float direction) {
        temp1.set(speed, 0);
        temp1.rotateDeg(direction);
        temp2.set(lateralSpeed, 0);
        temp2.rotateDeg(groundAngle - 90);
        temp1.add(temp2);
        temp1.rotateDeg(-(groundAngle - 90));
        lateralSpeed = temp1.x;
    }

    /**
     * Applies a force to the character that will propel them along the ground and in the air.
     * @param speed
     * @param direction
     */
    public void applyAirForce(float speed, float direction) {
        jumping = false;
        wallJumping = false;
        falling = true;
        canJump = false;
        coyoteTimer = 0;
        inputJumpJustPressed = 0;
        addMotion(speed, direction);
        lateralSpeed = deltaX;
    }

    /**
     * This method must be overridden to handle chracter controls before movement is applied to the character.
     */
    public abstract void handleControls();

    /**
     * This event is called for every frame the character is grounded and left or right input is received. This is not
     * called when the character is walkReversing.
     * @param delta
     * @param lateralSpeed
     * @param groundAngle
     * @see SlopeCharacterBehaviour#eventWalkReversing(float, float, float)
     */
    public abstract void eventWalking(float delta, float lateralSpeed, float groundAngle);

    /**
     * This event is called for every frame the character is grounded and has walking momentum, but is not applying left or
     * right input.
     * @param delta
     * @param lateralSpeed
     * @param groundAngle
     */
    public abstract void eventWalkStopping(float delta, float lateralSpeed, float groundAngle);

    /**
     * This event is called once the character has come to a halt while grounded.
     * @param delta
     */
    public abstract void eventWalkStop(float delta);

    /**
     * This event is called every frame that the character is grounded and is calling an input that makes them move in the
     * opposite direction of their momentum. This overrides the walking event.
     * @param delta
     * @param lateralSpeed
     * @param groundAngle
     * @see SlopeCharacterBehaviour#eventWalking(float, float, float)
     */
    public abstract void eventWalkReversing(float delta, float lateralSpeed, float groundAngle);

    /**
     * This event is called every frame when the character is calling an input that makes them walk up a slope that
     * would typically make them slide down. Only called if allowWalkUpSlides is true.
     * @param delta
     * @param lateralSpeed
     * @param groundAngle
     * @see SlopeCharacterBehaviour#allowWalkUpSlides
     */
    public abstract void eventWalkingSlide(float delta, float lateralSpeed, float groundAngle);

    /**
     * This event is called every frame when the character is pushing against a wall while walking.
     * @param delta
     * @param wallAngle
     */
    public abstract void eventWalkPushingWall(float delta, float wallAngle);

    /**
     * This event is called every frame when the character is standing close to an edge. This is determined by the
     * walkCliffEdgeDistance from the character's (x,y) position.
     * @param delta
     * @param right
     * @see SlopeCharacterBehaviour#walkCliffEdgeDistance
     */
    public abstract void eventCliffEdge(float delta, boolean right);

    /**
     * This event is called when a new ground fixture is touched while walking or falling and touching the ground.
     * @param fixture
     * @param contactNormalAngle
     * @param bounds
     * @param boundsData
     */
    public abstract void eventTouchGroundFixture(Fixture fixture, float contactNormalAngle, BoundsBehaviour bounds, BoundsData boundsData);

    /**
     * This event is called every frame while the character is sliding down a slope as defined by maxWalkAngle/maxSlideAngle.
     * @param delta
     * @param lateralSpeed
     * @param groundAngle
     * @param slidingAngle
     * @see SlopeCharacterBehaviour#maxWalkAngle
     * @see SlopeCharacterBehaviour#maxSlideAngle
     */
    public abstract void eventSlideSlope(float delta, float lateralSpeed, float groundAngle, float slidingAngle);

    /**
     * This event is called every frame while the character is pushing against a wall while sliding.
     * @param delta
     * @param wallAngle
     */
    public abstract void eventSlidePushingWall(float delta, float wallAngle);

    /**
     * This event is called once when the character initiates a jump.
     * @param delta
     */
    public abstract void eventJump(float delta);

    /**
     * This event is called once when the character releases the jump input. It is not called if the character continues to
     * hold the input past the apex of the jump.
     * @param delta
     * @see SlopeCharacterBehaviour#eventJumpApex(float)
     */
    public abstract void eventJumpReleased(float delta);

    /**
     * This event is called when the character moves while falling
     * @param lateralSpeed
     * @param delta
     */
    public abstract void eventFallMoving(float delta, float lateralSpeed);

    /**
     * This event is called once when the character reaches the apex of their jump.
     * @param delta
     */
    public abstract void eventJumpApex(float delta);

    /**
     * This event is called once when the character initiates a jump while sliding on a slope as defined by maxWalkAngle/maxSlideAngle.
     * @param delta
     * @see SlopeCharacterBehaviour#maxWalkAngle
     * @see SlopeCharacterBehaviour#maxSlideAngle
     * @see SlopeCharacterBehaviour#allowJumpingWhileSliding
     */
    public abstract void eventJumpFromSlide(float delta);

    /**
     * This event is called once when the character initiates a midair jump.
     * @param delta
     * @see SlopeCharacterBehaviour#midairJumps
     */
    public abstract void eventJumpMidair(float delta);

    /**
     * This event is called once when the chracter is jumping and hits a ceiling.
     * @param delta
     * @param ceilingAngle
     * @see SlopeCharacterBehaviour#maxCeilingAngle
     */
    public abstract void eventHitHead(float delta, float ceilingAngle);

    /**
     * This event is called every frame while the character is falliing in the air.
     * @param delta
     */
    public abstract void eventFalling(float delta);

    /**
     * This event is called every frame while the character is falling and touching a wall.
     * @param delta
     * @param wallAngle
     **/
    public abstract void eventFallingTouchingWall(float delta, float wallAngle);

    /**
     * This event is called once when the character has landed from a fall.
     * @param delta
     * @param groundAngle
     **/
    public abstract void eventLand(float delta, float groundAngle);

    /**
     * This event is called once when the character has first clinged to a wall.
     * @param delta
     * @param wallAngle
     **/
    public abstract void eventWallCling(float delta, float wallAngle);

    /**
     * This event is called once when the character has released from a wall.
     * @param delta
     */
    public abstract void eventReleaseWallCling(float delta);

    /**
     * This event is called every frame when the character is sliding down a wall.
     * @param delta
     * @param wallAngle
     **/
    public abstract void eventWallSliding(float delta, float wallAngle);

    /**
     * This event is called every frame when the character is climbing up or down a wall.
     * @param delta
     * @param wallAngle
     **/
    public abstract void eventWallClimbing(float delta, float wallAngle);

    /**
     * This event is called once when the character has climbed to the top of a wall and is propelled upwards.
     * @param delta
     **/
    public abstract void eventWallClimbReachedTop(float delta);

    /**
     * This event is called once when the character is clinging to a wall and initiates a jump.
     * @param delta
     * @param wallAngle
     **/
    public abstract void eventWallJump(float delta, float wallAngle);

    /**
     * This event is called once when the character grabs a ledge.
     * @param delta
     * @param wallAngle
     */
    public abstract void eventGrabLedge(float delta, float wallAngle);

    /**
     * This event is called once when the character releases from a ledge;
     * @param delta
     */
    public abstract void eventReleaseGrabLedge(float delta);

    /**
     * This event is called once when the character is clinging to a wall and initiates a jump.
     * @param delta
     * @param wallAngle
     */
    public abstract void eventLedgeJump(float delta, float wallAngle);

    /**
     * This event is called every frame when the character is pushing against a wall while moving horizontally when
     * clinging to a ceiling.
     * @param delta
     * @param wallContactAngle
     */
    public abstract void eventCeilingClingPushingWall(float delta, float wallContactAngle);

    /**
     * This event is called once the character has come to a halt while clinging to a ceiling.
     * @param delta
     */
    public abstract void eventCeilingClingStop(float delta);

    /**
     * This event is called for every frame the character is clinging to a ceiling and has horizontal momentum, but is
     * not applying left or right input.
     * @param previousSwingDelta
     * @param lateralSpeed
     * @param ceilingAngle
     */
    public abstract void eventCeilingClingStopping(float previousSwingDelta, float lateralSpeed, float ceilingAngle);

    /**
     * This event is called for every frame the character is clinging to a ceiling and left or right input is received.
     * This is not called when the character is moveReversing.
     * @param delta
     * @param lateralSpeed
     * @param ceilingAngle
     * @see SlopeCharacterBehaviour#eventCeilingClingMovingReversing(float, float, float)
     */
    public abstract void eventCeilingClingMoving(float delta, float lateralSpeed, float ceilingAngle);

    /**
     * This event is called every frame that the character is clinging to a ceiling and is calling an input that makes
     * them move in the opposite direction of their momentum. This overrides the moving event.
     * @param delta
     * @param lateralSpeed
     * @param groundAngle
     * @see SlopeCharacterBehaviour#eventCeilingClingMoving(float, float, float)
     */
    public abstract void eventCeilingClingMovingReversing(float delta, float lateralSpeed, float groundAngle);

    /**
     * This event is called once when the character releases from clinging to the ceiling.
     * @param delta
     */
    public abstract void eventCeilingClingReleased(float delta);
    /**
     * This event is called every frame when the character is pushing against a wall while in magnet mode.
     * @param delta
     * @param wallContactAngle
     */
    public abstract void eventMagnetPushingWall(float delta, float wallContactAngle);

    /**
     * This event is called once the character has come to a halt while in magnet mode.
     * @param delta
     */
    public abstract void eventMagnetStop(float delta);

    /**
     * This event is called for every frame the character is in magnet mode and has horizontal momentum, but is
     * not applying left or right input.
     * @param previousSwingDelta
     * @param lateralSpeed
     * @param ceilingAngle
     */
    public abstract void eventMagnetStopping(float previousSwingDelta, float lateralSpeed, float ceilingAngle);

    /**
     * This event is called for every frame the character is in magnet mode and left or right input is received.
     * This is not called when the character is moveReversing.
     * @param delta
     * @param lateralSpeed
     * @param ceilingAngle
     * @see SlopeCharacterBehaviour#eventCeilingClingMovingReversing(float, float, float)
     */
    public abstract void eventMagnetMoving(float delta, float lateralSpeed, float ceilingAngle);

    /**
     * This event is called every frame that the character is in magnet mode and is calling an input that makes
     * them move in the opposite direction of their momentum. This overrides the moving event.
     * @param delta
     * @param lateralSpeed
     * @param groundAngle
     * @see SlopeCharacterBehaviour#eventCeilingClingMoving(float, float, float)
     */
    public abstract void eventMagnetMovingReversing(float delta, float lateralSpeed, float groundAngle);

    /**
     * This event is called once when the character deactivates magnet mode.
     * @param delta
     */
    public abstract void eventMagnetReleased(float delta);
    /**
     * This event is called once when the character begins to pass through the bottom side of a passThrough bounds.
     * @param fixture
     * @param fixtureAngle
     * @param bounds
     * @param boundsData
     * @see BoundsBehaviour#canPassThroughBottom
     **/
    public abstract void eventPassedThroughPlatform(Fixture fixture, float fixtureAngle, BoundsBehaviour bounds, BoundsData boundsData);

    /**
     * This event is called once when the character initiates a swing while in the air.
     * @param delta
     * @param swingAngle
     * @param lateralSpeed
     **/
    public abstract void eventSwing(float delta, float swingAngle, float lateralSpeed);

    /**
     * This event is called every frame while the character is swinging.
     * @param delta
     * @param swingAngle
     * @param lateralSpeed
     **/
    public abstract void eventSwinging(float delta, float swingAngle, float lateralSpeed);

    /**
     * This event is called once when the character releases the input for a swing.
     * @param delta
     * @param swingAngle
     * @param lateralSpeed
     * @param automaticRelease
     **/
    public abstract void eventSwingReleased(float delta, float swingAngle, float lateralSpeed, boolean automaticRelease);

    /**
     * This event is called once when the swing is cancelled from the character colliding with a wall.
     * @param delta
     * @param swingAngle
     * @param lateralSpeed
     **/
    public abstract void eventSwingCrashWall(float delta, float swingAngle, float lateralSpeed);

    /**
     * This event is called once when the swing is cancelled from the character colliding with the ground.
     * @param delta
     * @param swingAngle
     * @param lateralSpeed
     **/
    public abstract void eventSwingCrashGround(float delta, float swingAngle, float lateralSpeed);

    /**
     * Handles the movement of the character after collision detection has been applied.
     * @param delta
     */
    private void handleMovement(float delta) {
        var body = getBody(this);
        //determine if the character is clinging to a wall.
        var lastClingingToWall = clingingToWall;
        boolean wallToRight = isEqual360(wallContactAngle, 180, 90);
        if (!allowClingToWalls || coyoteTimer > -clingToWallThreshold) clingingToWall = false;
        else {
            if (!inputWallClingRight && wallToRight || !inputWallClingLeft && !wallToRight || automaticallyClingToWalls) clingingToWall = false;
            if (falling && (touchingWall || lastClingingToWall)) {
                var clingingToRight = wallToRight && (inputWallClingRight || automaticallyClingToWalls);
                var clingingToLeft = !wallToRight && (inputWallClingLeft || automaticallyClingToWalls);
                if (clingingToRight || clingingToLeft) {
                    clingingToWall = false;
                    var rayX = body.getPosition().x + footRayOffsetX + (clingingToRight ? footRadius : -footRadius);
                    var rayY = body.getPosition().y + footRayOffsetY + wallClimbRayYoffset;
                    //raycast to one side to check for a wall
                    getUnBox().getWorld().rayCast((fixture, point, normal, fraction) -> {
                        if (!(fixture.getBody().getUserData() instanceof BoundsBehaviour)) return -1;
                        clingingToWall = true;

                        //attach if touching a moving platform
                        var bounds = (BoundsBehaviour) fixture.getBody().getUserData();
                        if (getBody(bounds).getType() == BodyType.KinematicBody) movingPlatformFixtures.add(fixture);
                        return 0;
                    }, rayX, rayY, rayX + (clingingToRight ? wallRayDistance : -wallRayDistance), rayY);
                }
            }
        }

        if (lastClingingToWall && !clingingToWall) eventReleaseWallCling(delta);

        //determine if the character is grabbing a ledge.
        var lastGrabbingLedge = grabbingLedge;
        var clingingInput = inputWallClingRight && wallToRight || inputWallClingLeft && !wallToRight;
        if (!allowGrabLedges || coyoteTimer > -grabLedgeThreshold || inputWallClimbDown) grabbingLedge = false;
        else if (lastGrabbingLedge && clingingInput) {
            grabbingLedge = true;
            ledgeGrabYadjustment = 0;
        } /*else if (lastClingingToWall) grabbingLedge = false;*/
        else {
            var climbingInput = inputWallClimbDown || inputWallClimbUp;
            if (!inputWallClingRight && wallToRight || !inputWallClingLeft && !wallToRight || automaticallyGrabLedges) grabbingLedge = false;
            if (falling && touchingWall && !climbingInput) {
                var clingingToRight = wallToRight && (inputWallClingRight || automaticallyGrabLedges);
                var clingingToLeft = !wallToRight && (inputWallClingLeft || automaticallyGrabLedges);
                if (clingingToRight || clingingToLeft) {
                    grabbingLedge = false;
                    var rayX = body.getPosition().x + footRayOffsetX + (clingingToRight ? footRadius : -footRadius);
                    var rayY = body.getPosition().y + footRayOffsetY + ledgeGrabYoffset;
                    //raycast to one side to check for a wall
                    getUnBox().getWorld().rayCast((fixture, point, normal, fraction) -> {
                        if (!(fixture.getBody().getUserData() instanceof BoundsBehaviour)) return -1;
                        var edgeShape = (EdgeShape) fixture.getShape();
                        edgeShape.getVertex1(temp1);
                        edgeShape.getVertex2(temp2);
                        var data = (BoundsData) fixture.getUserData();
                        //if fixture is not a wall
                        if (isEqual360(data.angle, 90, maxSlideAngle)) return -1;

                        var previousData = (BoundsData) data.previousFixture.getUserData();
                        var nextData = (BoundsData) data.nextFixture.getUserData();
                        var fixtureHighPoint = Math.max(temp1.y, temp2.y);
                        var distance = fixtureHighPoint - point.y;
                        var ledgeAngle = clingingToRight ? nextData.angle : previousData.angle;
                        if (distance < ledgeGrabMaxDistance && isEqual360(ledgeAngle, 90, ledgeGrabGroundMaxAngle)) {
                            ledgeGrabYadjustment = distance;
                            grabbingLedge = true;

                            //attach if touching a moving platform
                            var bounds = (BoundsBehaviour) fixture.getBody().getUserData();
                            if (getBody(bounds).getType() == BodyType.KinematicBody) movingPlatformFixtures.add(fixture);
                            return 0;
                        }
                        return -1;
                    }, rayX, rayY, rayX + (clingingToRight ? wallRayDistance : -wallRayDistance), rayY);
                }
            }
        }

        if (lastGrabbingLedge && !grabbingLedge) eventReleaseGrabLedge(delta);
        if (grounded && lastClingingToWall) {
            lateralSpeed = 0;
            eventReleaseWallCling(delta);
        }

        //check if the character is clinging to a ceiling fixture.
        lastClingingToCeiling = clingingToCeiling;
        clingingToCeiling = allowClingToCeilings && ceilingClingFixture != null && inputCeilingCling;
        if (!clingingToCeiling && lastClingingToCeiling && inputCeilingCling) {
            var rayX = body.getPosition().x + footRayOffsetX;
            var rayY = body.getPosition().y + footRayOffsetY + footRadius + torsoHeight;
            //raycast above to check for ceiling
            getUnBox().getWorld().rayCast((fixture, point, normal, fraction) -> {
                if (!(fixture.getBody().getUserData() instanceof BoundsBehaviour)) return -1;
                clingingToCeiling = true;
                ceilingClingFixture = fixture;
                ceilingAngle = ((BoundsData)fixture.getUserData()).angle;

                //attach if touching a moving platform
                var bounds = (BoundsBehaviour) fixture.getBody().getUserData();
                if (getBody(bounds).getType() == BodyType.KinematicBody) movingPlatformFixtures.add(fixture);
                return 0;
            }, rayX, rayY, rayX, rayY + ceilingRayDistance);
        }

        //check if the character is clinging to a fixture in magnet mode
        lastMagneting = magneting;
        if (!magneting && allowMagnet && inputMagnet && grounded) {
            magneting = true;
            magnetJumping = false;
        }

        if (magneting) {
            if (touchedMagnetFixtures.size == 0 && inputMagnet) {
                temp1.set(body.getPosition().x, body.getPosition().y + footRadius);
                temp2.set(magnetRayDistance, 0);
                temp2.rotateDeg((magnetWallAngle + 180) % 360);
                temp2.add(temp1);
                //raycast in magnet direction to check for fixture
                getUnBox().getWorld().rayCast((fixture, point, normal, fraction) -> {
                    if (!(fixture.getBody().getUserData() instanceof BoundsBehaviour)) return -1;
                    magnetFixture = fixture;
                    magnetWallAngle = ((BoundsData) fixture.getUserData()).angle;

                    //attach if touching a moving platform
                    var bounds = (BoundsBehaviour) fixture.getBody().getUserData();
                    if (getBody(bounds).getType() == BodyType.KinematicBody) movingPlatformFixtures.add(fixture);
                    return 0;
                }, temp1.x, temp1.y, temp2.x, temp2.y);
            }
        }

        //Clear attachment to a moving platform if no longer clinging to the side
        if (lastClingingToWall && !clingingToWall) {
            if (movingPlatformFixtures.size > 0) {
                movingPlatformFixtures.clear();
                lateralSpeed = deltaX;
            }
        }

        //determine if the swing should be stopped if allowSwingTerminationAtApex is activated.
        previousSwinging = swinging;
        swinging = inputSwing && falling && !touchingWall;
        if (previousSwinging && swinging && allowSwingTerminationAtApex && swingAnchorOrigin != null && !MathUtils.isZero(previousSwingDelta)) {
            if (Math.signum(swingDelta) != Math.signum(previousSwingDelta)) {
                swinging = false;
                eventSwingReleased(delta, swingAngle, lateralSpeed, true);
            }
        } else if (previousSwinging && !swinging) {
            if (touchingWall) eventSwingCrashWall(delta, swingAngle, lateralSpeed);
            else if (!falling) eventSwingCrashGround(delta, swingAngle, lateralSpeed);
            else eventSwingReleased(delta, swingAngle, lateralSpeed, false);
        }

        //Walking
        if (stickToGround && grounded && canWalkOnSlope && !falling && !magneting) {
            movementMode = WALKING;
            gravityY = 0;

            if (touchedGroundFixtures.size == 0) {
                setMotion(slopeStickForce, contactAngle + 180);
            }
            else setSpeed(0);

            var accelerating = false;
            var stopping = false;
            var pushingWall = false;
            if (inputRight || inputLeft) {
                var goRight = inputRight ? 1f : -1f;
                accelerating = Math.signum(lateralSpeed) == goRight;
                var acceleration = accelerating ? lateralAcceleration : lateralDeceleration;
                lateralSpeed = Utils.throttledAcceleration(lateralSpeed, goRight * lateralMaxSpeed, goRight * acceleration * delta, maintainExtraLateralMomentum);
            } else {
                lateralSpeed = Utils.throttledDeceleration(lateralSpeed, lateralMaxSpeed, lateralStopMinDeceleration * delta, lateralStopDeceleration * delta);
                stopping = true;
            }

            if (touchingWall) {
                if (lateralSpeed > 0 && isEqual360(wallContactAngle, 180, 90) || lateralSpeed < 0 && isEqual360(
                    wallContactAngle, 0, 90)) {
                    lateralSpeed = 0;
                    pushingWall = true;
                }
            }

            addMotion(lateralSpeed, contactAngle - 90f);

            if (justLanded) eventLand(delta, groundAngle);

            if (pushingWall) eventWalkPushingWall(delta, wallContactAngle);
            else if (stopping) {
                if (MathUtils.isZero(lateralSpeed)) eventWalkStop(delta);
                else eventWalkStopping(delta, lateralSpeed, groundAngle);
            } else {
                if (accelerating) eventWalking(delta, lateralSpeed, groundAngle);
                else eventWalkReversing(delta, lateralSpeed, groundAngle);
            }
        }
        //Sliding
        else if (stickToGround && grounded && !canWalkOnSlope && canSlideOnSlope && !falling && !magneting) {
            movementMode = SLIDING;
            gravityY = 0;

            if (justLanded) {
                temp1.set(deltaX, deltaY);
                temp1.rotateDeg(groundAngle + 90);
                lateralSpeed = temp1.x;
                clingingToWall = false;
            }
            if (touchedGroundFixtures.size == 0) {
                setMotion(slopeStickForce, contactAngle + 180);
            }
            else setSpeed(0);

            var slideDown = true;
            var walking = false;
            var pushingWall = false;

            if (justLanded) eventLand(delta, groundAngle);

            if (allowWalkUpSlides && (inputRight || inputLeft)) {
                slideDown = false;
                var goRight = inputRight ? 1f : -1f;
                var acceleration = Math.signum(lateralSpeed) == goRight ? lateralAcceleration : lateralDeceleration;
                lateralSpeed = Utils.throttledAcceleration(lateralSpeed, goRight * lateralMaxSpeed, goRight * acceleration * delta, maintainExtraLateralMomentum);
                walking = true;
            }

            if (slideDown) {
                if (isEqual360(contactAngle, 0, 90)) {
                    lateralSpeed = Utils.throttledAcceleration(lateralSpeed, lateralSlideMaxSpeed,
                        lateralSlideAcceleration * delta, maintainExtraLateralMomentum);
                } else {
                    lateralSpeed = Utils.throttledAcceleration(lateralSpeed, -lateralSlideMaxSpeed,
                        -lateralSlideAcceleration * delta, maintainExtraLateralMomentum);
                }
            }

            if (touchingWall) {
                if (lateralSpeed > 0 && isEqual360(wallContactAngle, 180, 90) || lateralSpeed < 0 && isEqual360(
                    wallContactAngle, 0, 90)) {
                    lateralSpeed = 0;
                    pushingWall = true;
                }
            }

            addMotion(lateralSpeed, contactAngle - 90f);

            if (pushingWall) eventSlidePushingWall(delta, wallContactAngle);
            else if (walking) eventWalkingSlide(delta, lateralSpeed, groundAngle);
            else eventSlideSlope(delta, lateralSpeed, groundAngle, contactAngle - 90f);
        }
        //Grabbing a ledge
        else if (grabbingLedge && !magneting) {
            movementMode = LEDGE_GRABBING;
            deltaX = 0;
            deltaY = 0;
            temp1.set(0, ledgeGrabYadjustment / delta);
            body.setLinearVelocity(temp1);
            gravityY = 0;
            if (!lastGrabbingLedge) eventGrabLedge(delta, wallContactAngle);
        }
        //Clinging to a wall
        else if (clingingToWall && !magneting) {
            movementMode = WALL_CLINGING;
            gravityY = 0;
            if (!lastClingingToWall) {
                eventWallCling(delta, wallFixtureAngle);
                lateralSpeed = deltaY;
                jumping = false;
            }
            setMotion(slopeStickForce, wallContactAngle + 180);

            var climbing = 0;
            if (allowClimbWalls && (inputWallClimbUp || inputWallClimbDown)) {
                climbing = inputWallClimbUp ? 1 : -1;
                lateralSpeed = Utils.throttledAcceleration(lateralSpeed, climbing * wallClimbMaxSpeed, climbing * wallClimbAcceleration * delta, false);
            } else {
                if (allowClimbWalls) lateralSpeed = Utils.throttledDeceleration(lateralSpeed, wallClimbMaxSpeed, wallClimbMinDeceleration, wallClimbDeceleration);
                else {
                    if (lateralSpeed > 0)
                        lateralSpeed = approach(lateralSpeed, 0, wallSlideUpDeceleration * delta);
                    else lateralSpeed += -Math.abs(wallSlideDownAcceleration) * delta;
                }

                var maxSpeed = Math.abs(wallSlideMaxSpeed);
                if (lateralSpeed < -maxSpeed) lateralSpeed = -maxSpeed;
            }

            addMotion(lateralSpeed, wallContactAngle + (wallToRight ? -90 : 90));

            if (climbing != 0) {
                eventWallClimbing(delta, wallFixtureAngle);
            } else {
                eventWallSliding(delta, wallFixtureAngle);
            }
        }
        //Clinging to a ceiling
        else if (clingingToCeiling && !magneting) {
            movementMode = CEILING_CLINGING;
            gravityY = 0;

            if (touchedCeilingClingFixtures.size == 0) {
                setMotion(slopeStickForce, ceilingAngle + 180);
            }
            else setSpeed(0);

            var accelerating = false;
            var stopping = false;
            var pushingWall = false;
            if (inputRight || inputLeft) {
                var goRight = inputRight ? 1f : -1f;
                accelerating = Math.signum(lateralSpeed) == goRight;
                var acceleration = accelerating ? ceilingClingLateralAcceleration : ceilingClingLateralDeceleration;
                lateralSpeed = Utils.throttledAcceleration(lateralSpeed, goRight * ceilingClingLateralMaxSpeed, goRight * acceleration * delta, maintainExtraLateralMomentum);
            } else {
                lateralSpeed = Utils.throttledDeceleration(lateralSpeed, ceilingClingLateralMaxSpeed, ceilingClingLateralStopMinDeceleration * delta, ceilingClingLateralStopDeceleration * delta);
                stopping = true;
            }

            if (touchingWall) {
                if (lateralSpeed > 0 && isEqual360(wallContactAngle, 180, 90) || lateralSpeed < 0 && isEqual360(
                    wallContactAngle, 0, 90)) {
                    lateralSpeed = 0;
                    pushingWall = true;
                }
            }

            addMotion(lateralSpeed, ceilingAngle + 90f);

            if (justLanded) eventLand(delta, ceilingAngle);

            if (pushingWall) eventCeilingClingPushingWall(delta, wallContactAngle);
            else if (stopping) {
                if (MathUtils.isZero(lateralSpeed)) eventCeilingClingStop(delta);
                else eventCeilingClingStopping(delta, lateralSpeed, ceilingAngle);
            } else {
                if (accelerating) eventCeilingClingMoving(delta, lateralSpeed, ceilingAngle);
                else eventCeilingClingMovingReversing(delta, lateralSpeed, ceilingAngle);
            }
        } else if (magneting) {
            movementMode = MAGNETING;
            gravityY = 0;

            if (!magnetJumping) {
                //the magnet ray didn't find a fixture, query the next/previous fixture
                if (magnetFixture == null && lastMagnetFixture != null) {
                    var boundsData = (BoundsData) lastMagnetFixture.getUserData();
                    var nextAngle = ((BoundsData) boundsData.nextFixture.getUserData()).angle;
                    var previousAngle = ((BoundsData) boundsData.previousFixture.getUserData()).angle;

                    if (isEqual360(boundsData.angle, 90, 45))
                        magnetWallAngle = deltaX > 0 ? nextAngle : previousAngle;
                    else if (isEqual360(boundsData.angle, 0, 45))
                        magnetWallAngle = deltaY <= 0 ? nextAngle : previousAngle;
                    else if (isEqual360(boundsData.angle, 270, 45))
                        magnetWallAngle = deltaX <= 0 ? nextAngle : previousAngle;
                    else magnetWallAngle = deltaY > 0 ? nextAngle : previousAngle;
                }

                if (magnetFixture == null && touchedTorsoMagnetFixtures.size == 0) {
                    magnetNoContactTimer -= delta;
                    if (magnetNoContactTimer <= 0) {
                        magneting = false;
                        magnetGoRight = 0;
                    }
                } else {
                    magnetNoContactTimer = magnetNoContactDelay;
                }

                if (touchedMagnetFixtures.size == 0) {
                    setMotion(slopeStickForce, magnetWallAngle + 180);
                } else setSpeed(0);

                var accelerating = false;
                var stopping = false;

                if (inputRight || inputLeft) {
                    if (magnetGoRight == 0) magnetGoRight = inputRight && isEqual360(magnetWallAngle, 90,
                        90) || inputLeft && isEqual360(magnetWallAngle, 270, 90) ? -1 : 1;
                    if (!lastMagneting) {
                        lateralSpeed = magnetGoRight * Math.abs(lateralSpeed);
                    }
                    accelerating = Math.signum(lateralSpeed) == magnetGoRight;
                    var acceleration = accelerating ? magnetLateralAcceleration : magnetLateralDeceleration;
                    lateralSpeed = Utils.throttledAcceleration(lateralSpeed, magnetGoRight * magnetLateralMaxSpeed,
                        magnetGoRight * acceleration * delta, maintainExtraLateralMomentum);
                } else {
                    magnetGoRight = 0;
                    lateralSpeed = Utils.throttledDeceleration(lateralSpeed, magnetLateralMaxSpeed,
                        magnetLateralStopMinDeceleration * delta, magnetLateralStopDeceleration * delta);
                    stopping = true;
                }

                addMotion(lateralSpeed, magnetWallAngle + 90f);

                if (justLanded) eventLand(delta, magnetWallAngle);

                if (stopping) {
                    if (MathUtils.isZero(lateralSpeed)) eventMagnetStop(delta);
                    else eventMagnetStopping(delta, lateralSpeed, ceilingAngle);
                } else {
                    if (accelerating) eventMagnetMoving(delta, lateralSpeed, magnetWallAngle);
                    else eventMagnetMovingReversing(delta, lateralSpeed, magnetWallAngle);
                }

                if (inputJump) {
                    magnetJumping = true;
                    jumping = true;
                    hitJumpApex = false;
                    falling = true;
                    canJump = false;
                    coyoteTimer = 0;
                    inputJumpJustPressed = 0;
                    movingPlatformFixtures.clear();
                    eventJump(delta);
                    setMotion(magnetJumpSpeed, magnetWallAngle);
                }
            } else {
                if (touchedTorsoMagnetFixtures.size == 0) {
                    magneting = false;
                    magnetGoRight = 0;
                }
            }
        }
        //Swinging
        else if (swinging) {
            movementMode = SWINGING;
            var anchorCharacterX = body.getPosition().x + swingCharacterAnchorOffsetX;
            var anchorCharacterY = body.getPosition().y + swingCharacterAnchorOffsetY;

            var justSwinged = false;
            //If just started swinging, create a new revoluteJoint
            if (inputSwingJustPressed && swingAnchorOrigin == null) {
                justSwinged = true;
                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyType.StaticBody;
                bodyDef.position.set(swingTargetX, swingTargetY);
                swingAnchorOrigin = getUnBox().getWorld().createBody(bodyDef);

                BodyDef bodyDef2 = new BodyDef();
                bodyDef2.type = BodyType.DynamicBody;
                bodyDef2.position.set(body.getPosition().x, body.getPosition().y);
                swingAnchorCharacter = getUnBox().getWorld().createBody(bodyDef2);

                PolygonShape polygonShape = new PolygonShape();
                polygonShape.setAsBox(10, 10);
                var fixture = swingAnchorOrigin.createFixture(polygonShape, 1f);
                fixture.getFilterData().categoryBits = CATEGORY_NO_CONTACT;
                fixture.getFilterData().maskBits = 0;

                fixture = swingAnchorCharacter.createFixture(polygonShape, 1f);
                fixture.getFilterData().categoryBits = CATEGORY_NO_CONTACT;
                fixture.getFilterData().maskBits = 0;
                polygonShape.dispose();

                RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
                revoluteJointDef.bodyA = swingAnchorOrigin;
                revoluteJointDef.bodyB = swingAnchorCharacter;
                revoluteJointDef.collideConnected = false;
                revoluteJointDef.localAnchorA.set(0, 0);
                revoluteJointDef.localAnchorB.set(swingTargetX - anchorCharacterX, swingTargetY - anchorCharacterY);
                revoluteJointDef.referenceAngle = MathUtils.degRad * pointDirection(swingTargetX, swingTargetY, anchorCharacterX, anchorCharacterY);
                swingJoint = (RevoluteJoint) getUnBox().getWorld().createJoint(revoluteJointDef);

                revoluteJointDef = new RevoluteJointDef();
                revoluteJointDef.bodyA = swingAnchorCharacter;
                revoluteJointDef.bodyB = body;
                revoluteJointDef.collideConnected = false;
                revoluteJointDef.localAnchorA.set(0, 0);
                revoluteJointDef.localAnchorB.set(swingCharacterAnchorOffsetX, swingCharacterAnchorOffsetY);
                getUnBox().getWorld().createJoint(revoluteJointDef);

//                controlBodyVelocity = false;
                temp1.set(swingImpulse, 0);
                temp1.rotateDeg(pointDirection(anchorCharacterX, anchorCharacterY, swingTargetX, swingTargetY) + (anchorCharacterX < swingTargetX ? -90 : 90));
                if (!swingMaintainVelocity) body.setLinearVelocity(temp1.x, temp1.y);
                else body.setLinearVelocity(body.getLinearVelocity().x + temp1.x, body.getLinearVelocity().y + temp1.y);
            }

            body.applyLinearImpulse(0, swingGravity * delta, anchorCharacterX, anchorCharacterY, true);
            body.setLinearVelocity(body.getLinearVelocity().x * (1 - swingFriction * delta), body.getLinearVelocity().y * (1 - swingFriction * delta));


            if (inputRight || inputLeft) {
                lateralSpeed = body.getLinearVelocity().x;
                var goRight = inputRight ? 1f : -1f;
                lateralSpeed = Utils.throttledAcceleration(lateralSpeed, goRight * lateralSwingMaxSpeed, goRight * lateralSwingAcceleration * delta, maintainExtraLateralMomentum);
                body.setLinearVelocity(lateralSpeed, body.getLinearVelocity().y);
            }

            if (justSwinged) eventSwing(delta, swingAngle, lateralSpeed);
            eventSwinging(delta, swingAngle,  lateralSpeed);
        }
        //Falling in the air (or jumping up)
        else {
            movementMode = FALLING;
            gravityY = gravity;

            if (wallJumpTimer < 0) wallJumping = false;

            if (inputRight || inputLeft) {
                var goRight = inputRight ? 1f : -1f;
                var acceleration = Math.signum(deltaX) == goRight ? lateralAirAcceleration : lateralAirDeceleration;
                if (wallJumping) acceleration = lateralAirWallJumpingAcceleration;
                deltaX = Utils.throttledAcceleration(deltaX, goRight * lateralAirMaxSpeed, goRight * acceleration * delta, maintainExtraLateralMomentum);
            } else {
                deltaX = Utils.throttledDeceleration(deltaX, lateralAirMaxSpeed, lateralAirStopMinDeceleration * delta, lateralAirStopDeceleration * delta);
            }
            lateralSpeed = deltaX;

            if (inputRight || inputLeft) {
                eventFallMoving(delta, lateralSpeed);
            }

            if (touchingWall && !hitHead) {
                if (deltaX > 0 && isEqual360(wallContactAngle, 180, 90)) deltaX = 0;
                else if (deltaX < 0 && isEqual360(wallContactAngle, 0, 90)) deltaX = 0;
            }

            if (hitHead) {
                if (deltaY > 0) deltaY = 0;
                eventHitHead(delta, ceilingAngle);
            }

            if (jumping && deltaY > 0 && !inputJump) {
                jumping = false;
                wallJumping = false;
                deltaY *= jumpReleaseDampening;
                eventJumpReleased(delta);
            }

            if (jumping && !hitJumpApex && deltaY <= 0) {
                hitJumpApex = true;
                eventJumpApex(delta);
            }

            var term = Math.abs(terminalVelocity);
            if (deltaY < -term) deltaY = -term;

            if (touchingWall) eventFallingTouchingWall(delta, wallContactAngle);
            else eventFalling(delta);
        }

        //destroy the swing anchor bodies which also causes the joint to be destroyed.
        if (previousSwinging && !swinging && swingAnchorOrigin != null) {
            getUnBox().getWorld().destroyBody(swingAnchorOrigin);
            swingAnchorOrigin = null;
            getUnBox().getWorld().destroyBody(swingAnchorCharacter);
            swingAnchorCharacter = null;

//            controlBodyVelocity = true;
            deltaX = body.getLinearVelocity().x;
            deltaY = body.getLinearVelocity().y;
            lateralSpeed = deltaX;
        }

        //determine if the character can jump
        canMidairJump = falling &&  coyoteTimer <= 0 && midairJumpTimer < 0 && (midairJumpCounter < midairJumps || midairJumps == -1) && !clingingToCeiling && !magneting;
        canWallJump = !grounded && (clingingToWall || allowWallJumpWithoutCling && touchingWall) && !clingingToCeiling && !magneting;
        canLedgeJump = !grounded && grabbingLedge && touchingWall && !clingingToCeiling && !magneting;
        if (allowJumpingWhileSliding) canJump = grounded && !falling && !clingingToCeiling && !magneting || coyoteTimer > 0 || canMidairJump;
        else canJump = grounded && !falling && canWalkOnSlope && !clingingToCeiling && !magneting || coyoteTimer > 0 || canMidairJump;

        //if reaching the top of a wall while wall climbing
        if (allowClingToWalls && allowClimbWalls && lastClingingToWall && !clingingToWall && inputWallClimbUp && deltaY > 0) {
            jumping = false;
            falling = true;
            canJump = false;
            coyoteTimer = 0;
            deltaY = wallClimbLedgeJumpSpeed;
            movingPlatformFixtures.clear();
            eventWallClimbReachedTop(delta);
        }

        //if grabbing a ledge and the character presses input to climb up
        if (allowGrabLedges && grabbingLedge && inputWallClimbUp) {
            jumping = false;
            falling = true;
            canJump = false;
            coyoteTimer = 0;
            deltaY = ledgeGrabJumpSpeed;
            movingPlatformFixtures.clear();
            eventWallClimbReachedTop(delta);
        }

        //if no longer clinging to the ceiling
        if (allowClingToCeilings && lastClingingToCeiling && !clingingToCeiling) {
            jumping = false;
            falling = true;
            canJump = false;
            coyoteTimer = 0;
            deltaY = 0;
            movingPlatformFixtures.clear();
            eventCeilingClingReleased(delta);
        }

        //if initiating a wall jump
        if (allowWallJump && canWallJump && !grabbingLedge && MathUtils.isEqual(inputJumpJustPressed, jumpTriggerDelay)) {
            jumping = true;
            hitJumpApex = false;
            wallJumping = true;
            wallJumpTimer = wallJumpDeactivateTime;
            falling = true;
            canJump = false;
            coyoteTimer = 0;
            inputJumpJustPressed = 0;
            midairJumpCounter++;
            movingPlatformFixtures.clear();
            setMotion(wallJumpSpeed, wallToRight ? 180 - wallJumpAngle : wallJumpAngle);
            eventWallJump(delta, wallContactAngle);
        }

        //if initiating a ledge jump
        if (allowLedgeJump && canLedgeJump && MathUtils.isEqual(inputJumpJustPressed, jumpTriggerDelay)) {
            jumping = true;
            hitJumpApex = false;
            wallJumping = true;
            wallJumpTimer = wallJumpDeactivateTime;
            falling = true;
            canJump = false;
            coyoteTimer = 0;
            inputJumpJustPressed = 0;
            midairJumpCounter++;
            movingPlatformFixtures.clear();
            setMotion(ledgeJumpSpeed, wallToRight ? 180 - ledgeJumpAngle : ledgeJumpAngle);
            eventLedgeJump(delta, wallContactAngle);
        }

        //if initiating a jump
        if (canJump && inputJumpJustPressed > 0) {
            jumping = true;
            hitJumpApex = false;
            falling = true;
            canJump = false;
            coyoteTimer = 0;
            inputJumpJustPressed = 0;
            movingPlatformFixtures.clear();
            if (inputLeft || inputRight) deltaX = lateralSpeed;
            deltaY = !canMidairJump ? jumpSpeed : midairJumpSpeed;
            if (canMidairJump) {
                midairJumpCounter++;
                midairJumpTimer = midairJumpDelay;
                eventJumpMidair(delta);
            } else {
                if (allowJumpingWhileSliding && !canWalkOnSlope) eventJumpFromSlide(delta);
                else eventJump(delta);
            }
        }

        //match the speed of the first attached moving platform.
        if (movingPlatformFixtures.size > 0) {
            var platform = (BoundsBehaviour) movingPlatformFixtures.first().getBody().getUserData();
            deltaX += platform.deltaX;
            deltaY += platform.deltaY;
        }

        //if touching a ground fixture and pressing input to pass through the floor.
        if (touchedGroundFixtures.size > 0 && inputPassThroughFloor) {
            for (var fixture : touchedGroundFixtures) {
                if (((BoundsBehaviour)fixture.getBody().getUserData()).canPassThroughBottom) {
                    falling = true;
                    jumping = false;
                    wallJumping = false;
                    canJump = false;
                    coyoteTimer = 0;
                    inputJumpJustPressed = 0;
                    break;
                }
            }
        }
    }

    @Override
    public void render(Batch batch) {
        if (showDebug) {
            if (!magneting) {
                //foot ray
                shapeDrawer.setColor(Color.GREEN);
                shapeDrawer.setDefaultLineWidth(.05f);
                shapeDrawer.line(getBody(this).getPosition().x + footRayOffsetX, getBody(this).getPosition().y + footRayOffsetY, getBody(this).getPosition().x + footOffsetX,
                    getBody(this).getPosition().y + footOffsetY - footRayDistance);
            } else {
                //magnet ray
                temp1.set(getBody(this).getPosition().x, getBody(this).getPosition().y + footRadius);
                temp2.set(magnetRayDistance, 0);
                temp2.rotateDeg((magnetWallAngle + 180) % 360);
                temp2.add(temp1);
                shapeDrawer.setColor(Color.GREEN);
                shapeDrawer.setDefaultLineWidth(5f);
                shapeDrawer.line(temp1.x, temp1.y, temp2.x, temp2.y);
            }

            //ceiling ray
            shapeDrawer.setColor(Color.GREEN);
            shapeDrawer.setDefaultLineWidth(.05f);
            var rayX = getBody(this).getPosition().x + footRayOffsetX;
            var rayY = getBody(this).getPosition().y + footRayOffsetY + torsoHeight + footRadius;
            shapeDrawer.line(rayX, rayY, rayX, rayY + ceilingRayDistance);

            //wall rays
            rayX = getBody(this).getPosition().x + footRayOffsetX + footRadius;
            rayY = getBody(this).getPosition().y + footRayOffsetY + wallClimbRayYoffset;
            shapeDrawer.line(rayX, rayY, rayX + wallRayDistance, rayY);
            rayX = getBody(this).getPosition().x + footRayOffsetX - footRadius;
            shapeDrawer.line(rayX, rayY, rayX - wallRayDistance, rayY);

            //ledge rays
            rayX = getBody(this).getPosition().x + footRayOffsetX + footRadius;
            rayY = getBody(this).getPosition().y + footRayOffsetY + ledgeGrabYoffset;
            shapeDrawer.line(rayX, rayY, rayX + wallRayDistance, rayY);
            rayX = getBody(this).getPosition().x + footRayOffsetX - footRadius;
            shapeDrawer.line(rayX, rayY, rayX - wallRayDistance, rayY);

            //contact angle
            shapeDrawer.setColor(Color.RED);
            shapeDrawer.setDefaultLineWidth(.05f);
            temp1.set(.2f, 0);
            temp1.rotateDeg(contactAngle);
            shapeDrawer.line(getBody(this).getPosition().x, getBody(this).getPosition().y, getBody(this).getPosition().x + temp1.x, getBody(this).getPosition().y + temp1.y);

            //ground angle
            shapeDrawer.setColor(Color.BLUE);
            shapeDrawer.setDefaultLineWidth(.05f);
            temp1.set(.2f, 0);
            temp1.rotateDeg(groundAngle);
            shapeDrawer.line(getBody(this).getPosition().x, getBody(this).getPosition().y, getBody(this).getPosition().x + temp1.x, getBody(this).getPosition().y + temp1.y);
        }
    }

    @Override
    public void onCollisionEnter(Behaviour other, Contact contact) {
        var fixture = getBody(this) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();
        var otherFixture = getBody(other) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();

        var bounds = other.getGameObject().getBehaviour(BoundsBehaviour.class);
        if (bounds != null) {
            if (magneting && fixture == torsoFixture) {
                contact.setEnabled(false);
                touchedTorsoMagnetFixtures.add(otherFixture);
                return;
            }
            var manifold = contact.getWorldManifold();
            var boundsData = (BoundsData) otherFixture.getUserData();
            float normalAngle = manifold.getNormal().angleDeg();
            float fixtureAngle = boundsData.angle;

            //Add the canPassThroughBottom BoundsBehaviour to the passThrough list if the character is
            // * already passing through it with another fixture
            // * is hitting his head on it
            // * is inside the BoundsBehaviour and hitting the fixture from the inside
            if (bounds.canPassThroughBottom) {
                var alreadyContacting = !checkContactEnabledPassThrough(bounds);
                var hittingHead = falling && !isEqual360(normalAngle, 90, maxSlideAngle);
                var badAngle = isEqual360(normalAngle, fixtureAngle + 180, 90);
                if (alreadyContacting || hittingHead || badAngle) {
                    passThroughFixtures.add(otherFixture);
                    eventPassedThroughPlatform(otherFixture, fixtureAngle, bounds, boundsData);
                    if (fixture == torsoFixture || fixture == footFixture) {
                        contact.setEnabled(false);
                        return;
                    }
                }
            }

            //Add the fixture to the list of touched ground fixtures if it is a walkable or slideable surface
            if (fixture == footFixture && isEqual360(fixtureAngle, 90, maxSlideAngle)) {
                if (clearLastTouchedGroundFixtures) {
                    lastTouchedGroundFixtures.clear();
                    clearLastTouchedGroundFixtures = false;
                }
                touchedGroundFixtures.add(otherFixture);
                lastTouchedGroundFixtures.add(otherFixture);
                eventTouchGroundFixture(otherFixture, normalAngle, bounds, boundsData);
            }

            //Add the fixture to the list of touched ceiling fixture if it is within ceiling angle
            if (fixture == torsoFixture && isEqual360(fixtureAngle, 270, maxCeilingAngle)) {
                touchedCeilingClingFixtures.add(otherFixture);
            }

            if (fixture == footFixture && magneting) {
                touchedMagnetFixtures.add(otherFixture);
            }

            //add the fixture to the list of moving platforms if the BoundsBehaviour is kinematic
            if (getBody(bounds).getType() == BodyType.KinematicBody) {
                movingPlatformFixtures.add(otherFixture);
            }
        }
    }

    private boolean checkContactEnabledPassThrough(BoundsBehaviour boundsBehaviour) {
        if (!boundsBehaviour.canPassThroughBottom) return true;
        for (Fixture fixture : passThroughFixtures) {
            if (boundsBehaviour == fixture.getBody().getUserData()) return false;
        }

        return true;
    }

    @Override
    public boolean onCollisionPreSolve(Behaviour other, Contact contact, Manifold oldManifold) {
        var fixture = getBody(this) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();
        var otherFixture = getBody(other) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();

        var bounds = other.getGameObject().getBehaviour(BoundsBehaviour.class);
        if (bounds != null) {
            if (magneting && fixture == torsoFixture) {
                contact.setEnabled(false);
                return false;
            }
            var manifold = contact.getWorldManifold();
            float normalAngle = manifold.getNormal().angleDeg();
            var otherFixtureData = ((BoundsData) otherFixture.getUserData());
            float fixtureAngle = otherFixtureData.angle;

            //pass through the floor if pressing input and bounds.canPassThroughBottom == true
            if (inputPassThroughFloor && bounds.canPassThroughBottom) {
                contact.setEnabled(false);
                if (!passThroughFixtures.contains(otherFixture, true)) passThroughFixtures.add(otherFixture);
                return false;
            }

            if (fixture == torsoFixture && bounds.canPassThroughBottom) {
                contact.setEnabled(false);
                return true;
            }

            if (!checkContactEnabledPassThrough(bounds)) {
                contact.setEnabled(false);
                return false;
            }

            if (fixture == footFixture) {
                canWalkOnSlope = false;
                canSlideOnSlope = false;

                if (isEqual360(fixtureAngle, 90, maxSlideAngle) && deltaY < 0) {
                    if (falling) {
                        falling = false;
                        jumping = false;
                        wallJumping = false;
                        midairJumpCounter = 0;
                        justLanded = true;
                    }
                }

                contact.setFriction(0f);
                if (allowMagnet) {
                    magnetWallAngle = normalAngle;
                    magnetFixture = otherFixture;
                }
            } else if (fixture == torsoFixture) {
                contact.setFriction(0f);

                if (isEqual360(normalAngle, 270, maxCeilingAngle)) {
                    hitHead = true;
                    ceilingAngle = normalAngle;
                    if (bounds.ceilingClingable) ceilingClingFixture = otherFixture;
                }

                //if not a walkable, slideable, or ceiling angle, it's a wall.
                if (!isEqual360(normalAngle, 90, maxSlideAngle) && !isEqual360(normalAngle, 270, maxCeilingAngle)) {
                    touchingWall = true;
                    wallContactAngle = normalAngle;
                    wallFixtureAngle = otherFixtureData.angle;
                }
            }
        }
        return false;
    }

    @Override
    public void onCollisionExit(Behaviour other, Contact contact) {
        var fixture = getBody(this) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();
        var otherFixture = getBody(other) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();

        var bounds = other.getGameObject().getBehaviour(BoundsBehaviour.class);
        if (bounds != null) {
            if (!checkContactEnabledPassThrough(bounds)) {
                passThroughFixtures.removeValue(otherFixture, true);
            }

            if (fixture == footFixture) {
                touchedGroundFixtures.remove(otherFixture);
                touchedMagnetFixtures.remove(otherFixture);
            }

            if (fixture == torsoFixture) {
                touchedCeilingClingFixtures.remove(otherFixture);
                touchedTorsoMagnetFixtures.remove(otherFixture);
            }
        }
    }

    @Override
    public boolean onCollisionPostSolve(Behaviour other, Contact contact, ContactImpulse impulse) {
        var fixture = getBody(this) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();
        var otherFixture = getBody(other) == contact.getFixtureA().getBody() ? contact.getFixtureA() : contact.getFixtureB();

        var bounds = other.getGameObject().getBehaviour(BoundsBehaviour.class);
        if (bounds != null) {
            if (!checkContactEnabledPassThrough(bounds)) {
                return false;
            }

            float angle = contact.getWorldManifold().getNormal().angleDeg();
            if (fixture == footFixture) {
                if (isEqual360(angle, 90, maxSlideAngle)) contactAngle = angle;

                if (!canSlideOnSlope || !canWalkOnSlope) {
                    groundAngle = ((BoundsData) otherFixture.getUserData()).angle;
                    slopeCheck();
                }
            }
        }
        return false;
    }

    public void setMotion(float speed, float direction) {
        temp1.set(speed, 0);
        temp1.rotateDeg(direction);
        deltaX = temp1.x;
        deltaY = temp1.y;
    }

    public void addMotion(float speed, float direction) {
        temp1.set(speed, 0);
        temp1.rotateDeg(direction);
        deltaX += temp1.x;
        deltaY += temp1.y;
    }

    public void setSpeed(float speed) {
        setMotion(speed, getDirection());
    }

    public float getSpeed() {
        temp1.set(deltaX, deltaY);
        return temp1.len();
    }

    public float getDirection() {
        temp1.set(deltaX, deltaY);
        return temp1.angleDeg();
    }

    public void setDirection(float direction) {
        setMotion(getSpeed(), direction);
    }
}
