

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.document.TextField;


import java.awt.*;


import java.io.File;



public class Test {


    @org.junit.Test
    public void test1(){

        System.out.println("haha");
    }
  /**
   * 建立索引
   *
   * 第一步：创建一个java工程，并导入jar包。
   第二步：创建一个indexwriter对象。
   1）指定索引库的存放位置Directory对象
   2）指定一个IndexWriterConfig对象。
   第二步：创建document对象。
   第三步：创建field对象，将field添加到document对象中。
   第四步：使用indexwriter对象将document对象写入索引库，此过程进行索引创建。并将索引和document对象写入索引库。
   第五步：关闭IndexWriter对象。
   * */
    @org.junit.Test
    public void createIndex() throws Exception {
        //指定索引库存放的路径
        //D:\temp\index  D:\java\index
        Directory directory = FSDirectory.open(new File("D:\\java\\index").toPath());
        //索引库还可以存放到内存中
        //Directory directory = new RAMDirectory();
        //创建indexwriterCofig对象
        IndexWriterConfig config = new IndexWriterConfig();
        //创建indexwriter对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        //原始文档的路径
        File dir = new File("C:\\Users\\Administrator\\Desktop\\体验课(录播课)课件\\B第二梯队\\Lucene和solr\\searchsource");
        for (File f : dir.listFiles()) {
            //文件名
            String fileName = f.getName();
            //文件内容
            String fileContent = FileUtils.readFileToString(f,"utf-8");
            //文件路径
            String filePath = f.getPath();
            //文件的大小
            long fileSize  = FileUtils.sizeOf(f);
            //创建文件名域
            //第一个参数：域的名称
            //第二个参数：域的内容
            //第三个参数：是否存储
            Field fileNameField = new TextField("filename", fileName, Field.Store.YES);
            //文件内容域
            Field fileContentField = new TextField("content", fileContent, Field.Store.YES);
            //文件路径域（不分析、不索引、只存储）
            Field filePathField = new TextField("path", filePath, Field.Store.YES);
            //文件大小域
            Field fileSizeField = new TextField("size", fileSize + "", Field.Store.YES);

            //创建document对象
            Document document = new Document();
            document.add(fileNameField);
            document.add(fileContentField);
            document.add(filePathField);
            document.add(fileSizeField);
            //创建索引，并写入索引库
            indexWriter.addDocument(document);
        }
        //关闭indexwriter
        indexWriter.close();


    }



    /**
    *5.3查询索引
     *
     *第一步：创建一个Directory对象，也就是索引库存放的位置。
     第二步：创建一个indexReader对象，需要指定Directory对象。
     第三步：创建一个indexsearcher对象，需要指定IndexReader对象
     第四步：创建一个TermQuery对象，指定查询的域和查询的关键词。
     第五步：执行查询。
     第六步：返回查询结果。遍历查询结果并输出。
     第七步：关闭IndexReader对象
     *
     *
    * */
    //查询索引库
    @org.junit.Test
    public void searchIndex() throws Exception {
        //指定索引库存放的路径
        //D:\temp\index
        Directory directory = FSDirectory.open(new File("D:\\java\\index").toPath());
        //创建indexReader对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建indexsearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //创建查询
        Query query = new TermQuery(new Term("filename", "apache"));
        //执行查询
        //第一个参数是查询对象，第二个参数是查询结果返回的最大值
        TopDocs topDocs = indexSearcher.search(query, 10);
        //查询结果的总条数
        System.out.println("查询结果的总条数："+ topDocs.totalHits);
        //遍历查询结果
        //topDocs.scoreDocs存储了document对象的id
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //scoreDoc.doc属性就是document对象的id
            //根据document的id找到document对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("filename"));
            //System.out.println(document.get("content"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
            System.out.println("-------------------------");
        }
        //关闭indexreader对象
        indexReader.close();
    }



    //查看标准分析器的分词效果
    @org.junit.Test
    public void testTokenStream() throws Exception {
        //创建一个标准分析器对象
        Analyzer analyzer = new StandardAnalyzer();
        //获得tokenStream对象
        //第一个参数：域名，可以随便给一个
        //第二个参数：要分析的文本内容
        TokenStream tokenStream = analyzer.tokenStream("test", "The Spring Framework provides a comprehensive programming and configuration model.");
        //添加一个引用，可以获得每个关键词
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        //添加一个偏移量的引用，记录了关键词的开始位置以及结束位置
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        //将指针调整到列表的头部
        tokenStream.reset();
        //遍历关键词列表，通过incrementToken方法判断列表是否结束
        while(tokenStream.incrementToken()) {
            //关键词的起始位置
            System.out.println("start->" + offsetAttribute.startOffset());
            //取关键词
            System.out.println(charTermAttribute);
            //结束位置
            System.out.println("end->" + offsetAttribute.endOffset());
        }
        tokenStream.close();
    }

/**6.2.1Lucene自带中文分词器
StandardAnalyzer：
单字分词：就是按照中文一个字一个字地进行分词。如：“我爱中国”，
效果：“我”、“爱”、“中”、“国”。
SmartChineseAnalyzer
对中文支持较好，但扩展性差，扩展词库，禁用词库和同义词库等不好处理
*/






}
