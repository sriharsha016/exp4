import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.text.DecimalFormat;
import java.util.*;

public class DatacenterSingleHostSingleCloudlet {

    private static List<Vm> vmList;
    private static List<Cloudlet> cloudletList;

    public static void main(String[] args) {

        Log.printLine("========== Starting CloudSim Simulation ==========");

        try {
            /* --------------------------------------------------
             * STEP 1: Initialize CloudSim
             * -------------------------------------------------- */
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            /* --------------------------------------------------
             * STEP 2: Create Datacenter
             * -------------------------------------------------- */
            Datacenter datacenter = createDatacenter("Datacenter_1");

            /* --------------------------------------------------
             * STEP 3: Create Broker
             * -------------------------------------------------- */
            DatacenterBroker broker = new DatacenterBroker("Broker_1");
            int brokerId = broker.getId();

            /* --------------------------------------------------
             * STEP 4: Create Virtual Machine
             * -------------------------------------------------- */
            vmList = new ArrayList<>();

            int vmId = 0;
            int mips = 1000;
            long size = 10000; // MB
            int ram = 512;     // MB
            long bw = 1000;
            int pesNumber = 1;
            String vmm = "Xen";

            Vm vm = new Vm(
                    vmId,
                    brokerId,
                    mips,
                    pesNumber,
                    ram,
                    bw,
                    size,
                    vmm,
                    new CloudletSchedulerTimeShared()
            );

            vmList.add(vm);
            broker.submitVmList(vmList);

            /* --------------------------------------------------
             * STEP 5: Create Cloudlet
             * -------------------------------------------------- */
            cloudletList = new ArrayList<>();

            int cloudletId = 0;
            long length = 400000;
            long fileSize = 300;
            long outputSize = 300;

            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet cloudlet = new Cloudlet(
                    cloudletId,
                    length,
                    pesNumber,
                    fileSize,
                    outputSize,
                    utilizationModel,
                    utilizationModel,
                    utilizationModel
            );

            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(vmId);

            cloudletList.add(cloudlet);
            broker.submitCloudletList(cloudletList);

            /* --------------------------------------------------
             * STEP 6: Start Simulation
             * -------------------------------------------------- */
            CloudSim.startSimulation();

            /* --------------------------------------------------
             * STEP 7: Stop Simulation
             * -------------------------------------------------- */
            CloudSim.stopSimulation();

            /* --------------------------------------------------
             * STEP 8: Display Results
             * -------------------------------------------------- */
            List<Cloudlet> resultList = broker.getCloudletReceivedList();
            printCloudletResults(resultList);

            Log.printLine("========== CloudSim Simulation Finished ==========");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Simulation terminated due to an error");
        }
    }

    /**
     * Creates a Datacenter with one Host
     */
    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<>();

        /* ---------------- Host Configuration ---------------- */
        int mips = 1000;
        int pesNumber = 1;
        int ram = 2048;          // MB
        long storage = 1000000;  // MB
        int bw = 10000;

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        Host host = new Host(
                0,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        );

        hostList.add(host);

        /* ---------------- Datacenter Characteristics ---------------- */
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics =
                new DatacenterCharacteristics(
                        arch,
                        os,
                        vmm,
                        hostList,
                        timeZone,
                        cost,
                        costPerMem,
                        costPerStorage,
                        costPerBw
                );

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(
                    name,
                    characteristics,
                    new VmAllocationPolicySimple(hostList),
                    new ArrayList<Storage>(),
                    0
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Prints Cloudlet Execution Results
     */
    private static void printCloudletResults(List<Cloudlet> list) {

        DecimalFormat dft = new DecimalFormat("###.##");
        String indent = "    ";

        System.out.println("\n========== CLOUDLET EXECUTION OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent +
                "Datacenter ID" + indent + "VM ID" + indent +
                "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            System.out.print(indent + cloudlet.getCloudletId());

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                System.out.print(indent + "SUCCESS");
                System.out.print(indent + cloudlet.getResourceId());
                System.out.print(indent + cloudlet.getVmId());
                System.out.print(indent + dft.format(cloudlet.getExecStartTime()));
                System.out.println(indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}
