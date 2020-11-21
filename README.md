# 经典自动化测试大作业
> 181250096 陆志晗

## 项目结构

按照要求，本项目分为Demo，Report和Project三个文件夹，分别存放可运行jar包testSelection.jar，针对第一组数据生成 10 个代码依赖图（PDF 和dot格式），以及项目代码。

在项目代码中，CentralNode.java包含项目入口main函数，生成分析域，读写文件等方法，ByClass.java和ByMethod.java则是以两种粒度筛选受变更的的测试。

## 运行说明

运行后会在jar包所在目录下生成class(method).dot和selection-class(method).txt两个文件，前者为中间产物，后者为约定输出，每行一个方法签名，表示一个受测试影响的方法。

