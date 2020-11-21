import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.*;


public class CentralNode {
    //项目入口
    public static void main(String[] args) throws ClassHierarchyException, CancelException, IOException, InvalidClassFileException {
        //输入格式：java -jar testSelection.jar -c/-m <project_target> <change_info>
        //<project_target>指向待测项目的target文件夹；<change_info>指向记录了变更信息的文本文件
        String type = args[0];
        String project_target = args[1];
        String change_info = args[2];
//        //调试用
//        String type = "-c";
//        String project_target = "C:\\Users\\luzhi\\Desktop\\大三上\\自动化测试\\作业\\大作业\\经典大作业\\Automated-Testing\\Data\\5-MoreTriangle\\target";
//        String change_info = "C:\\Users\\luzhi\\Desktop\\大三上\\自动化测试\\作业\\大作业\\经典大作业\\Automated-Testing\\Data\\5-MoreTriangle\\data\\change_info.txt";
//        System.out.println("Input: "+type+" "+project_target+" "+change_info);

        //生成分析域
        AnalysisScope scope = scopeBuilder(project_target);

        //生成类层次关系对象
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        // 生成进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        // 利用CHA算法构建调用图
        CHACallGraph cg = new CHACallGraph(cha);
        //调用图初始化
        cg.init(eps);

        //change_info内容读取
        ArrayList<String> message = readFile(change_info);

        //类和方法两种粒度分析
        if(type.equals("-c")){
            ByClass.byClass(cg,message);
        }
        else {
            ByMethod.byMethod(cg,message);
        }
    }


    //生成分析域
    public static AnalysisScope scopeBuilder(String project_target) throws IOException, InvalidClassFileException {
        AnalysisScope scope;
        ArrayList<File> classes;
        ArrayList<File> testClasses;

        //统一格式，去除末尾的"/"防止出错
        if (project_target.endsWith("/")) {
            project_target = project_target.substring(0, project_target.length() - 1);
        }

        //scope包含classes和test-classes两个文件夹里的class文件
        scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), AnalysisScope.class.getClassLoader());
        classes = getFiles(project_target + "/classes");
        assert classes != null;
        for (File file : classes) {
            scope.addClassFileToScope(ClassLoaderReference.Application, file);
        }
        testClasses = getFiles(project_target + "/test-classes");
        assert testClasses != null;
        for (File file : testClasses) {
            scope.addClassFileToScope(ClassLoaderReference.Application, file);
        }
        return scope;
    }

    //获取文件
    public static ArrayList<File> getFiles(String target) {
        File file = new File(target);
        String[] paths = file.list();
        ArrayList<File> result = new ArrayList<>();

        if (paths == null) {
            return null;
        }

        for (String path : paths) {
            File tempFile = new File(target + "/" + path);
            //是文件夹，递归
            if (tempFile.isDirectory()) {
                ArrayList<File> temp = getFiles(target + "/" + path);
                if (temp != null) {
                    result.addAll(temp);
                }
            } else {
                //取得文件
                if(path.endsWith(".class")) {
                    result.add(tempFile);
                }
            }
        }
        return result;
    }

    //文件内容读取
    public static ArrayList<String> readFile(String change_info){
        ArrayList<String> message = new ArrayList<>();
        try (FileReader reader = new FileReader(change_info);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                message.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    //将结果输出到txt文件中
    public static void writeFile(ArrayList<String> content,String path){
        try (FileWriter fw = new FileWriter(path);
             BufferedWriter bw = new BufferedWriter(fw)
        ) {
            for(int i=0;i<content.size();i++){
                if(i==content.size()-1)
                    bw.write(content.get(i));
                else
                    bw.write(content.get(i)+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
