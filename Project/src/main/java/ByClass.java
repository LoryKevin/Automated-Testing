import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;

import java.util.ArrayList;
import java.util.Iterator;


public class ByClass {
    //缓存.dot内容
    public static ArrayList<String> classDot = new ArrayList<>();
    //缓存类间依赖信息
    public static ArrayList<String> classRelation = new ArrayList<>();
    //缓存被选出的类
    public static ArrayList<String> selectClass = new ArrayList<>();

    public static void byClass(CHACallGraph cg, ArrayList<String> change_info){
        ArrayList<String> allClass = new ArrayList<>();
        ArrayList<String> changedClass = new ArrayList<>();

        //生成class.dot
        getClassDot(cg);

        //README里的方法
        for(CGNode node:cg){
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if (node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    // 获取方法签名
                    String signature = method.getSignature();
                    if(!signature.contains("<init>") &&!signature.contains("initialize()") && !allClass.contains(classInnerName+" "+signature)){
                        allClass.add(classInnerName+" "+signature);
                    }
                }
            }
        }

        //筛选受影响的类
        for(String relation:classRelation){
            String classInnerName = relation.split(" ")[0]; //前一个类
            String nextClassInnerName = relation.split(" ")[1]; //后一个类
            for(String change:change_info){
                if(classInnerName.equals(change.split(" ")[0]) && nextClassInnerName.contains("Test")){
                    changedClass.add(nextClassInnerName);
                }
            }
        }
        for(String allclass : allClass){
            if(changedClass.contains(allclass.split(" ")[0])){
                selectClass.add(allclass);
            }
        }

        CentralNode.writeFile(selectClass,"selection-class.txt");
        System.out.println("selection-class.txt build complete");
    }

    //生成class.dot
    public static void getClassDot(CHACallGraph cg){
        classDot.add("digraph class {"); //title
        //README里的方法
        for(CGNode node:cg){
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if (node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    // 获取方法签名
                    String signature = method.getSignature();
                    if(signature.contains("Test") || signature.contains("$")) continue;
                    Iterator<CGNode> predNodes= cg.getPredNodes(node);
                    while(predNodes.hasNext()){
                        CGNode nextNode = predNodes.next();
                        if("Application".equals(nextNode.getMethod().getDeclaringClass().getClassLoader().toString())) {
                            String nextClassInnerName=nextNode.getMethod().getDeclaringClass().getName().toString();
                            if(!classDot.contains("\t\""+classInnerName+"\" -> \""+nextClassInnerName+"\";")){ //尚未存在的记录
                                classDot.add("\t\""+classInnerName+"\" -> \""+nextClassInnerName+"\";");
                                classRelation.add(classInnerName+" "+nextClassInnerName);
                            }
                        }
                    }
                }
            }
        }

        classDot.add("}"); //end
        CentralNode.writeFile(classDot,"class.dot");
        System.out.println("class.dot build complete");
    }

}
