package com.mmall.util;import org.apache.commons.net.ftp.FTPClient;import org.apache.commons.net.ftp.FTPFile;import org.apache.commons.net.ftp.FTPReply;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.io.*;import java.net.SocketException;public class FtpUtil {    private static Logger logger = LoggerFactory.getLogger(FtpUtil.class);    private static String host = PropertiesUtil.getProperty("ftp.server.ip");    private static Integer port = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));    private static String username = PropertiesUtil.getProperty("ftp.user");    private static String password = PropertiesUtil.getProperty("ftp.pass");    private static String basePath = PropertiesUtil.getProperty("ftp.basepath");    private static String LOCAL_CHARSET = "GBK";    private static String SERVER_CHARSET = "ISO-8859-1";    public static void setCharset(String param) {        LOCAL_CHARSET = param;    }    public static boolean uploadFile(String filePrefix, String fileName, InputStream input) throws IOException {        boolean result = false;        // 新建一个FTP客户端对象引用        FTPClient ftpClient = null;        try {            ftpClient = connectAndLoginFTP();            // 将文件名编码转换            fileName = new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET);            String path = basePath + filePrefix;            //切换到上传目录            if (!ftpClient.changeWorkingDirectory(path)) {                //如果目录不存在创建目录                String[] dirs = path.split("/");                String tempPath = "";                for (String dir : dirs) {                    if (null == dir || "".equals(dir)) {                        continue;                    }                    tempPath += "/" + dir;                    if (!ftpClient.changeWorkingDirectory(tempPath)) {                        logger.error("目录不存在，将创建目录！:{}", tempPath);                        if (!ftpClient.makeDirectory(tempPath)) {                            logger.error("{},文件目录创建失败！", tempPath);                            return result;                        }                    }                }                logger.info("切换到上传文件目录！：{}", tempPath);                ftpClient.changeWorkingDirectory(tempPath);            }            logger.info("正在上传文件！");            //上传文件            if (!ftpClient.storeFile(fileName, input)) {                logger.error("上传文件失败！");                // 上传失败返回false。                return result;            }            logger.info("上传文件成功！");            input.close();            // 注销登录            logger.info("正在注销登录！");            ftpClient.logout();            // 执行到这，说明已经上传成功了，将返回值设置为true            result = true;        } catch (SocketException e) {            logger.error("出错了：{}", e);        } catch (IOException e) {            logger.error("出错了：{}", e);        } finally {            try {                // 若客户端是连接着FTP服务器的话进行断开连接                if (null != ftpClient && ftpClient.isConnected()) {                    logger.info("正在断开连接！");                    ftpClient.disconnect();                    ftpClient = null;                }            } catch (IOException e) {                logger.error("出错了：{}", e);            }        }        return result;    }    /**     * Description: 从FTP服务器下载文件     *     * @param remotePath FTP服务器上的相对路径     * @param fileName   要下载的文件名     * @param localPath  下载后保存到本地的路径     * @return     */    public static boolean downloadFile(String remotePath, String fileName, String localPath) {        boolean result = false;        FTPClient ftpClient = null;        try {            ftpClient = connectAndLoginFTP();            ftpClient.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录            FTPFile[] fs = ftpClient.listFiles();            for (FTPFile ff : fs) {                if (ff.getName().equals(fileName)) {                    File localFile = new File(localPath + "/" + ff.getName());                    OutputStream is = new FileOutputStream(localFile);                    ftpClient.retrieveFile(ff.getName(), is);                    is.close();                }            }            ftpClient.logout();            result = true;        } catch (SocketException e) {            logger.error("连接出错了：{}", e);        } catch (IOException e) {            logger.error("出错了：{}", e);        } finally {            if (ftpClient.isConnected()) {                try {                    ftpClient.disconnect();                } catch (IOException ioe) {                    logger.error("出错了：{}", ioe);                }            }        }        return result;    }    private static FTPClient connectAndLoginFTP() throws SocketException, IOException {        // 新建一个FTP客户端对象引用        FTPClient ftpClient = null;        if (ftpClient == null) {            ftpClient = new FTPClient();        }        logger.info("正在连接ftp服务器！");        // 进行连接FTP服务器        ftpClient.connect(host, port);        // 获取服务器的回复码        int replyCode = ftpClient.getReplyCode();        // 当回复码的值在[200 ,300)区间的时候说明连接成功        if (FTPReply.isPositiveCompletion(replyCode)) {            logger.info("正在登录ftp服务器！");            // 连接成功后进行登录            if (ftpClient.login(username, password)) {                // 若未指定字符集，登录成功后进行发送命令，开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.                if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {                    LOCAL_CHARSET = "UTF-8";                } else {                    // 传递的UTF-8不支持，则使用本地编码                    LOCAL_CHARSET = "GBK";                }                ftpClient.setControlEncoding(LOCAL_CHARSET);                // 设置被动模式                ftpClient.enterLocalPassiveMode();                // 设置传输的模式return;                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);            } else {                logger.error("登录失败！");                // 说明登录失败抛出异常                throw new RuntimeException("Connet ftpServer error! Please check user or password");            }        }        return ftpClient;    }}