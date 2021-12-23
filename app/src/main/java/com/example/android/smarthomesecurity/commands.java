package com.example.android.smarthomesecurity;

import com.example.android.smarthomesecurity.FireSubsystem.AlarmFireCommand;
import com.example.android.smarthomesecurity.GasSubsystem.GasValveCommand;
import com.example.android.smarthomesecurity.HouseBreakingSubsystem.AlarmHouseCommand;
import com.example.android.smarthomesecurity.HouseBreakingSubsystem.DoorMotorCommand;
import com.example.android.smarthomesecurity.HouseBreakingSubsystem.closeDoor;
import com.example.android.smarthomesecurity.WaterSubsystem.DrainCommand;
import com.example.android.smarthomesecurity.WaterSubsystem.WaterValveCommand;

public class commands {


    public void fireAlarmOn(){
        new AlarmFireCommand("1").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Fire+"/commands/alarm");
    }
    public void fireAlarmOff(){
        new AlarmFireCommand("0").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Fire+"/commands/alarm");
    }
    public void drainOn(){
        new DrainCommand("1").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Water+"/commands/drain");
    }
    public void drainOff(){
        new DrainCommand("0").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Water+"/commands/drain");
    }
    public void waterValveOn(){
        new WaterValveCommand("1").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Water+"/commands/watervalve");
    }
    public void waterValveOff(){
        new WaterValveCommand("0").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Water+"/commands/watervalve");
    }
    public void gasValveOn(){
        new GasValveCommand("on").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Gas+"/commands/gas_open");
    }
    public void gasValveOff(){
        new GasValveCommand("off").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Gas+"/commands/gas_close");
    }
//    public void suctionOn(){
//        new SuctionCommand("1").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Gas+"/commands/suction");
//    }
//    public void suctionOff(){
//        new SuctionCommand("0").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Gas+"/commands/suction");
//    }
    public void openDoor(){
        new DoorMotorCommand().execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Fire+"/commands/open_door");
    }
    public void closeDoor(){
        new closeDoor().execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_Fire+"/commands/open_door");
    }
    public void houseAlarmOn(){
        new AlarmHouseCommand("1").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_HouseBreaking+"/commands/alarm");
    }
    public void houseAlarmOff(){
        new AlarmHouseCommand("0").execute("https://cloud.kaaiot.com/cex/api/v1/endpoints/"+Constants.endpointId_HouseBreaking+"/commands/alarm");
    }




}
