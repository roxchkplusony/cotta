package net.sf.cotta.ftp;

import net.sf.cotta.TDirectory;
import net.sf.cotta.TFileFactory;
import net.sf.cotta.TFileNotFoundException;
import net.sf.cotta.io.InputManager;
import net.sf.cotta.io.InputProcessor;
import net.sf.cotta.io.OutputManager;
import net.sf.cotta.io.OutputProcessor;
import net.sf.cotta.test.assertion.CodeBlock;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class TestFtpServerFileSystemTest extends FtpTestCase {
  public void testBeAtRootAndNoFileInitially() throws InterruptedException, IOException {
    String workingDirectory = ftpClient.printWorkingDirectory();
    ensure.that(workingDirectory).eq("/");
    String[] listedNames = ftpClient.listNames();
    ensure.that(listedNames).eq();
  }

  public void testBeAbleToListFiles() throws IOException {
    rootDir.file("testFile").ensureExists();
    String[] listedNames = ftpClient.listNames();
    ensure.that(listedNames).eq("testFile");
  }

  public void testBeAbleToListDirectories() throws IOException {
    rootDir.file("testDir").ensureExists();
    String[] listedNames = ftpClient.listNames();
    ensure.that(listedNames).eq("testDir");
  }

  public void testBeAbleToMakeDirectory() throws IOException {
    boolean success = ftpClient.makeDirectory("testDir");
    ensure.that(success).isTrue();
    List<TDirectory> listedDirs = rootDir.list().dirs();
    ensure.that(listedDirs).eq(rootDir.dir("testDir"));
  }

  public void testBeAbleToRemoveDirectory() throws IOException {
    ftpClient.makeDirectory("testDir");
    ftpClient.removeDirectory("testDir");
    ensure.that(rootDir.list().dirs()).eq();
  }

  public void testBeAbleToChangeWorkingDirectory() throws IOException {
    ftpClient.makeDirectory("testDir");
    ftpClient.cwd("testDir");
    String workingDirectory = ftpClient.printWorkingDirectory();
    ensure.that(workingDirectory).eq("/testDir");
  }

  public void testBeAbleToStoreFile() throws IOException {
    final byte[] fileContent = createTestFileContent();
    ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
    ftpClient.storeFile("testFile", new ByteArrayInputStream(fileContent));
    rootDir.file("testFile").read(new InputProcessor() {
      public void process(InputManager inputManager) throws IOException {
        byte[] fileContentRead = IOUtils.toByteArray(inputManager.inputStream());
        ensure.that(fileContentRead).eq(fileContentRead);
      }
    });
  }

  public void testFileNotFoundOnServer() throws IOException {
    ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
    ensure.code(new CodeBlock() {
      public void execute() throws Exception {
        TFileFactory factory = new TFileFactory(new FtpFileSystem(ftpClient));
        factory.file("notexist").load();
      }
    }).throwsException(TFileNotFoundException.class);
  }

  public void testBeAbleToRetrieveFile() throws IOException {
    final byte[] fileContent = createTestFileContent();
    rootDir.file("testFile").write(new OutputProcessor() {
      public void process(OutputManager manager) throws IOException {
        IOUtils.write(fileContent, manager.outputStream());
      }
    });
    ByteArrayOutputStream fileContentReadBuffer = new ByteArrayOutputStream();
    ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
    ftpClient.retrieveFile("testFile", fileContentReadBuffer);
    ensure.that(fileContentReadBuffer.toByteArray()).eq(fileContent);
  }

  public void testBeAbleToRenameFile() throws IOException {
    rootDir.file("testFile").save("");
    ftpClient.rename("testFile", "renamedTestFile");
    ensure.that(rootDir.list().files()).eq(rootDir.file("renamedTestFile"));
  }

  public void testBeAbleToRenameDirectory() throws IOException {
    rootDir.dir("testDir").ensureExists();
    ftpClient.rename("testDir", "renamedTestDir");
    ensure.that(rootDir.list().dirs()).eq(rootDir.dir("renamedTestDir"));
  }

  public void testBeAbleToChangeToParentDirectory() throws IOException {
    ftpClient.makeDirectory("TestChangeToPaqrentDirectory");
    ftpClient.cwd("TestChangeToPaqrentDirectory");
    ftpClient.changeToParentDirectory();
    ensure.that(ftpClient.printWorkingDirectory()).eq("/");
  }

  private byte[] createTestFileContent() {
    ByteArrayOutputStream fileContentBuffer = new ByteArrayOutputStream();
    for (int i = 1; i < 1000; i++) {
      fileContentBuffer.write(i % 255);
    }
    return fileContentBuffer.toByteArray();
  }
}
