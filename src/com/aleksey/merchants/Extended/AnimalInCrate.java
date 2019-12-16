package com.aleksey.merchants.Extended;

import static com.aleksey.merchants.Extended.Integration.ItemCrateClass;
import static com.aleksey.merchants.Extended.Integration.isAnimalCrateModLoaded;
import com.bioxx.tfc.Core.TFC_Time;
import com.bioxx.tfc.api.TFCOptions;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;



/**
 *
 * @author Swarg
 * for Compare Crate with animal 
 * "taeog.animalcrate.item.ItemCrate"
 */
public class AnimalInCrate {    
    
    public static final String ANIMAL = "Animal";
    public static final String ID = "id";
    public static final String SEX = "Sex";
    public static final String AGE = "Age";        
    public static final String MSPEED = "MateSpeed";
    public static final String MJUMP = "MateJump";
    //public static final String MHEALTH = "MateHealth";? Health
    public static final String VARIANT = "Variant";
    public static final String FAMILIARITY = "Familiarity";//35 100
    public static final String ATTRIBUTES = "Attributes";    
    public static final String GMAXHEALTH = "generic.maxHealth";
    public static final String GMOVEMENTSPEED = "generic.movementSpeed";
    public static final String GJUMPSTRENGTH = "horse.jumpStrength";
    public static boolean allowSetOnlyTFCAnimal = true;
    public static String TFCAnimalsList = "";
    public static final String[] InvalidTFCAnimal = {"skeletonTFC","zombieTFC","spiderTFC","slimeTFC","ghastTFC","caveSpiderTFC","blazeTFC", "endermanTFC","pigZombieTFC","boarTFC","banditTFC","minecartTFC","arrowTFC","standTFC","creeperTFC","irongolemTFC"};
        //the values from th TFC they have not constants
    public static final String[] ANIMALSNAMES = {"bearTFC", "chickenTFC", "cowTFC", "deerTFC", "horseTFC", "pigTFC", "sheepTFC", "wolfTFC"};
    public static final float[] ANIMALSTIMETOADULT = {60, 4.14f, 36, 24, 30, 15, 12, 9};

    
    public static final int UKNOWN = 0;
    public static final int ABABY = 1;
    public static final int AADULT = 2;
    
    public int id;
    public String name;
    public int sex;//+1
    public int age;
    public float speed;
    private int speedX10; //123 is 12.3 m/s
    public int jumpHX10;//25  is 2.5m jumpHeigth*10
    public float jumpStrength;
    public int health;
    public int familiarity;
    public int variant;
    
    
   
    public static boolean isValidAnimalCrate(ItemStack stack)
    {
      //return (stack!=null && isAnimalCrateModLoaded() && stack.getItem() instanceof taeog.animalcrate.item.ItemCrate);
      return ( stack!=null && isAnimalCrateModLoaded() && 
              ItemCrateClass != null && stack.getItem().getClass() == ItemCrateClass
              //stack.getItem().getClass().toString().contains("taeog.animalcrate.item.ItemCrate")
              );
    }
    
    
    /**
     * Create by AnimalCrateItemStack NBT
     */ 
    public AnimalInCrate(NBTTagCompound nbt)
    {
        this.id = 0;
        this.sex = 0;
        this.age = 0;
        this.speed = 0;
        this.jumpStrength = 0;        
        this.speedX10 = 0;
        this.jumpHX10 = 0;        
        this.familiarity = 0;
        this.variant = 0;        
        
        if (nbt == null)
            return;
        
        nbt = nbt.getCompoundTag(ANIMAL);
        if (nbt == null)
            return;
        
        name = nbt.getString(ID);//horseTFC
        id = getIdByAnimalName(name);
        if ( nbt.hasKey(SEX) )
                sex = nbt.getInteger(SEX); //0 man 1 female 2 - any for buying up
        
        age = nbt.getInteger(AGE);//real is birthday days left from start
        if (age != 1 && age != 2) //here age is Age stat: 1 - baby 2 adult, not birthday!
            age = (age == 0 ) ? 0 : getAnimalAge(name, age);// 0 - UKNOWN 1 - BABY 2 - ADULT
                    
        variant = nbt.getInteger(VARIANT);//surface
        familiarity = nbt.getInteger(FAMILIARITY);//35 cap
        
        NBTTagList anbt = nbt.getTagList(ATTRIBUTES, 10);
        if( anbt == null || anbt.tagCount() < 1)
            return;
        
        for (int i = 0; i < anbt.tagCount(); i++) 
        {
            NBTTagCompound att = anbt.getCompoundTagAt(i);            
            String _name = att.getString("Name");            
            
            if (_name != null && _name.contains(GMAXHEALTH) )
                this.health = (int) att.getDouble("Base"); 
            if (_name != null && _name.contains(GMOVEMENTSPEED) )
            {
                this.speed = (float) att.getDouble("Base");
                if (this.speed>0)
                    this.speedX10 = (int) Math.floor( this.speed * 430 );
            }
            else if (_name != null && _name.endsWith(GJUMPSTRENGTH))
            {
                this.jumpStrength = (float) att.getDouble("Base");
                if (this.jumpStrength > 0)
                    this.jumpHX10 = (int) Math.floor( getJumpHeight(this.jumpStrength) * 10);
                if (speed > 0)
                    break;                
            }    
        }   
    }
    
    /**
     * Create AnimCrate to compare on payMode by GUI params
     */
    public AnimalInCrate(
            int p1,  //id
            int p2,  //familiarity * 100 + Age*10 + sex;
            int p3,  //speed + jump  * 1000;
            int p4   //a.variant;
    )
    {
        this.id = 0;
        this.sex = 0;
        this.age = 0;
        this.speed = 0;
        this.jumpStrength = 0;        
        this.speedX10 = 0;
        this.jumpHX10 = 0;        
        this.familiarity = 0;
        this.variant = 0;        
        
        if (p1 <= 0) {
            return;
        }        
        this.id = p1;
        
        if (p2 > 0)//0 sex=man
        {
            this.sex = p2 < 10 ? p2 : p2 % 10;
            if (this.sex > 2 || this.sex < 0)
                sex = 2;
            this.age =  (p2 >= 10)? (int) Math.floor( p2 / 10 ) % 10 : 0;
            if (this.age > 2)
                this.age = 2;
            this.familiarity = (p2 >= 100)? (int) Math.floor( p2 / 100 ) : 0;
            if (this.familiarity > 100)
                this.familiarity = 100;
        }    
        
        if ( p3 > 0)
        {
            this.speedX10 = (p3 > 0)? p3 % 1000: 0;
            this.speed = (this.speedX10 > 0) ? (float) this.speedX10 / 430 : 0 ;
            this.jumpHX10 = (p3 > 1000)? (int) Math.floor(p3 / 1000): 0;
            this.jumpStrength = (float) getJumpStrength(jumpHX10);
        }
        
        this.variant = p4;
    } 
    
    public static EditPayParams getParamsForAnimalCrate(ItemStack iStack)
    {
        if (iStack==null || iStack.stackTagCompound == null)
            return null;
        
        AnimalInCrate animal = new AnimalInCrate(iStack.stackTagCompound);
        if (animal.id > 0 )
        {
            int p1 = animal.id;
            int p2 = animal.sex + animal.age*10 + animal.familiarity * 100; // 3521 35-famil 2-Adult 1-sex(0-man 1-female 2-any)            
            int speed = animal.speedX10;            
            int jump = animal.jumpHX10;
            int p3 = speed + jump  * 1000;//45103 is jump 4.5m speed 10.3m/s
            int p4 = animal.variant;
            return new EditPayParams(p1,p2,p3,p4);
        }
        return null;
    }
    
    /**
     * Parse String Field to Params for EditPayGUI toolTip     
     */
    public static EditPayParams getAnimalSexAgeFamiliarity(String str)
    {
      if (str == null || str.isEmpty())
          return null;
      
      int p2 = ExtendedLogic.strToInt(str);
      
      if (p2 < 0)
          return null;
      
      int sex =0;
      int age = 0;
      int familiarity = 0;
      
      if (p2 > 0) {
          sex = p2 < 10? p2 : p2 % 10;
          age =  (p2 >= 10)? (int) Math.floor( p2 / 10  ) % 10 : 0;
          if (age > 2 )
              age =2;      
          familiarity = (p2 >= 100)? (int) Math.floor( p2 / 100 ) : 0;
          if ( familiarity > 100)
              familiarity = 100;
      }
      
      return new EditPayParams(sex,age,familiarity,0);
    }
    
    /**
    * Parse String Field to Params for EditPayGUI toolTip     
    */
    public static EditPayParams getAnimalJumpSpeed(String str)
    {
      if (str == null || str.isEmpty())
          return null;
      int p3 = ExtendedLogic.strToInt(str);       
      
      int speedX10 = (p3 > 0)? p3 % 1000: 0;
      int jumpHX10 = (p3 > 1000)? (int) Math.floor(p3 / 1000): 0;
      return new EditPayParams(jumpHX10, speedX10, 0,0);        
    }
    
    /**
     * For GUI setPayitem
     */
    public NBTTagCompound writeToNBT()
    {
        if ( this.id <= 0 )
            return null;
        
        NBTTagCompound nbt = new NBTTagCompound();
        String name = getAnimalNameByID(id);        
        if ( name == null || name.isEmpty() || 
                //can set only valid tfc animal
                ( allowSetOnlyTFCAnimal && !isValidTFCAnimal(name) ) )
            return null;
            
        nbt.setString(ID, name);                
        nbt.setInteger(SEX, sex);//0 man 1 female  2 - any for buying up        
        nbt.setInteger(AGE, age);
        nbt.setInteger(VARIANT, variant);
        nbt.setInteger(FAMILIARITY,familiarity);
        
        if ( this.speed > 0 )//("horseTFC")
        {
            NBTTagList attrList = new NBTTagList();
            
            NBTTagCompound attrSpeed = new NBTTagCompound();            
            attrSpeed.setFloat("Base", this.speed );
            attrSpeed.setString("Name",GMOVEMENTSPEED);        
            attrList.appendTag(attrSpeed);
            
            NBTTagCompound attrJump = new NBTTagCompound();            
            if ( this.jumpStrength > 0 )
            {
                attrJump.setFloat("Base", this.jumpStrength );
                attrJump.setString("Name",GJUMPSTRENGTH);
                attrList.appendTag(attrJump);                
            }
            nbt.setTag(ATTRIBUTES, attrList);
        }
        NBTTagCompound anim = new NBTTagCompound();
        anim.setTag(ANIMAL, nbt);
        return anim;    
    }
        
    /**
     * Animal, logic Age UKNOWN ADULT BABY
     */
    public static int getAnimalAge(String name,int birthday)
    {
        if (name == null|| name.isEmpty() )
            return UKNOWN;
        int days = getDaysForAdultByAnimalName(name);
        int totalDays = TFC_Time.getTotalDays();        
        return (totalDays - birthday >= days )? AADULT : ABABY;        
    }
            
    
    public static int getDaysForAdultByAnimalName(String name)
    {
        if ( name == null || name.isEmpty() )
            return 0;
        
        if (ANIMALSTIMETOADULT.length != ANIMALSNAMES.length)
            return 0;
        
        for (int i = 0; i < ANIMALSNAMES.length; i++) {
            String n = ANIMALSNAMES[i];
            if (name.compareTo(ANIMALSNAMES[i])==0)
                return (int) Math.floor( TFCOptions.animalTimeMultiplier 
                        * TFC_Time.daysInMonth * ANIMALSTIMETOADULT[i] ) ;
        }
        return 0;
    }
    
    
    /**
     * Suited enimal to trade. True if animal two have better feature 
     * @param a animal two
     * @return 
     */
    public boolean isAnimalEqual(AnimalInCrate a)
    {        
        if ( a ==null || this.id==0)
            return false;
        
        return ( this.id == a.id 
                //if the value of sex is not defined(0) allow trade animal with any sex
                && ( this.sex==2 || this.sex == a.sex  )//2 - any for buyinug 0 man 1 femal
                && (this.age==0 || this.age == a.age)
                && (this.familiarity==0 || this.familiarity <= a.familiarity)
                
                && (this.speed==0 || this.speed <= a.speed)
                && (this.jumpStrength==0 || this.jumpStrength <= a.jumpStrength)
                                
                && (this.variant==0 || this.variant == a.variant)
                );        
    }
    
    
    public static int getIdByAnimalName(String name)
    {
        if (name == null || name.isEmpty()  )
            return 0;
        //EntityList.classToStringMapping.containsKey(ids)
        Class oclass = (Class)EntityList.stringToClassMapping.get(name);
        if (oclass == null)
            return 0;
        Set set = EntityList.IDtoClassMapping.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();                  
            if (mentry.getValue()==oclass) {
                return (Integer) mentry.getKey();                    
            }    
        }            
        return 0;
    }
    
    public static String getAnimalNameByID(int id)
    {
        if (id<=0)
            return "";
        Class oclass = (Class) EntityList.IDtoClassMapping.get(Integer.valueOf(id));
        //EntityList.classToStringMapping.containsKey(ids)
        //Class oclass = (Class)stringToClassMapping.get(name);
        if (oclass == null)
            return "";
        return (String) EntityList.classToStringMapping.get(oclass);        
    }
    
    //from https://github.com/fubira/HorseInfoReloaded/blob/master/src/main/java/net/ironingot/horseinfo/HorseInfoUtil.java
    public static double getJumpHeight(double jumpStrength) {
        double yVelocity = jumpStrength;
        double jumpHeight = 0.0d;

        while (yVelocity > 0.0D) {
            jumpHeight += yVelocity;
            yVelocity -= 0.08D;
            yVelocity *= 0.98D;
        }
        return Math.floor(jumpHeight * 10.0D) / 10.0D ;
    }
    /**
     * back getJumpHeight jump Height to Strength
     * @param jumpHeight from GUI *10 like 55 is 5.5m;  floor 5
     * @return JumpStrength
     */
    public static double getJumpStrength(int jumpHeightX10)
    {
        if (jumpHeightX10 == 0)
            return 0;
        
        float[] strength = new float[] { 
            0.0f,    // 0  0 
            0.2490f, // 1  0.5  05
            0.3690f, // 2  1.0  10
            0.4640f, // 3  1.5  15
            0.5450f, // 4  2.0  20
            0.6180f, // 5  2.5
            0.6850f, // 6  3.0
            0.7460f, // 7  3.5
            0.8040f, // 8  4.0
            0.8580f, // 9  4.5
            0.9100f, //10  5.0
            0.9600f, //11  5.5
            1.0080f // 12  6.0
        };
        int d = jumpHeightX10 / 5;
        return (d < 1 || d >= strength.length)? 0: strength[d];        
    }
    
    public static String getListOfAnimals()
    {        
        if ( TFCAnimalsList==null || TFCAnimalsList.isEmpty() )
        {
            int h = EntityList.IDtoClassMapping.size();
            String r = "";
            for (int i = 0; i < h; i++) {
                String name = getAnimalNameByID(i);
                if ( isValidTFCAnimal(name) )
                      r += String.format("%s  %s\n", Integer.toString(i), name);
                
            }
            TFCAnimalsList = r; 
        }
        return TFCAnimalsList;
    }
    
    public static boolean isValidTFCAnimal(String name)
    {
        if (name==null || name.isEmpty() || ! name.contains("TFC"))
            return false;
        
        for (int i = 0; i < InvalidTFCAnimal.length; i++) 
        {
            String badName = InvalidTFCAnimal[i];
            if (name.contains(badName))
                return false;            
        }        
        return true;
    }
    
    public static String getItemKeyForAnimalCrate(ItemStack iStack, String key)
    {
      if (iStack == null || !iStack.hasTagCompound() || !isValidAnimalCrate(iStack) )
          return key;
      
      AnimalInCrate animal = new AnimalInCrate(iStack.stackTagCompound);
      
      return (animal==null || animal.id <= 0 ) ? key : 
              key+":"+animal.id+":"+
              Integer.toString(animal.sex)+
              Integer.toString(animal.age)+
              Integer.toString(animal.familiarity)+
              Integer.toString(animal.speedX10)+
              Integer.toString(animal.variant);
    }
    
    
  
}
