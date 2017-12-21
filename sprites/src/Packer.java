import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Json;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Packer
{
    @Parameter(description = "[Input dir] [Output dir]")
    public List<String> files = new ArrayList<String>(0);

    public File inputDir;
    public File outputDir;

    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help;

    @Parameter(names = {"--fast", "-f"})
    private boolean fast;

    @Parameter(names = {"--name", "-n"})
    private String atlasName;

    @Parameter(names = {"--config", "-c"})
    private String configName;

    @Parameter(names = {"-scale", "-s"})
    private Float scale;

    public static void main(String[] args)
    {
        Packer packer = new Packer();
        JCommander jcommander = new JCommander(packer, args);

        if (packer.help) {
            jcommander.usage();
            System.exit(0);
        }

        if (packer.files.size() == 0) {
            packer.inputDir = new File(".").getAbsoluteFile();
            packer.outputDir = new File(packer.inputDir, "output");
        } else if (packer.files.size() == 1) {
            packer.inputDir = new File(packer.files.get(0));
            if (!packer.inputDir.exists()) {
                System.out.println("Input dir does not exist");
                System.exit(1);
            }
            packer.inputDir = packer.inputDir.getAbsoluteFile();
            packer.outputDir = new File(packer.inputDir, "output");
        } else if (packer.files.size() == 2) {
            packer.inputDir = new File(packer.files.get(0));
            if (!packer.inputDir.exists()) {
                System.out.println("Input dir does not exist");
                System.out.println(new File("").getAbsolutePath());
                System.exit(1);
            }
            packer.inputDir = packer.inputDir.getAbsoluteFile();

            packer.outputDir = new File(packer.files.get(1));
            if (!packer.outputDir.exists()) {
                packer.outputDir.mkdirs();
            }
            packer.outputDir = packer.outputDir.getAbsoluteFile();
        } else {
            jcommander.usage();
            System.exit(1);
        }

        packer.run();
    }

    public void run()
    {

        TexturePacker.Settings settings = null;

        if(configName != null) {
            try {
                settings = new Json().fromJson(TexturePacker.Settings.class, new FileReader(new File(configName)));
            } catch (FileNotFoundException e) {
                System.out.println("Config file does not exist");
                System.exit(1);
            }
        } else {
            try {
                settings = new Json().fromJson(TexturePacker.Settings.class, new FileReader(new File(inputDir,  "pack.json")));
            }
            catch (FileNotFoundException e)
            {
                settings = new TexturePacker.Settings();
            }
        }

        if (scale != null) {
            settings.scale = new float[] {scale};
            settings.scaleSuffix = new String[] {""};
        }

        settings.fast = fast;

        String filename;
        if (atlasName == null)
            filename = "sprites.atlas";
        else
            filename = atlasName + ".atlas";

        TexturePacker.process(settings, inputDir.getAbsolutePath(), outputDir.getAbsolutePath(), filename);

    }
}
