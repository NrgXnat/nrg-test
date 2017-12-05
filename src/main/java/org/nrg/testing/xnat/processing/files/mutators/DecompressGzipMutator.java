package org.nrg.testing.xnat.processing.files.mutators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class DecompressGzipMutator extends FileMutator {

    public DecompressGzipMutator() {}

    @Override
    public File mutateFile(File file) {
        String name = file.getPath();
        name = name.substring(0, name.length() - 3);

        try {
            byte[] buffer = new byte[1024];
            GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(file));
            FileOutputStream outputStream = new FileOutputStream(name);

            int bytes;
            while ((bytes = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytes);
            }

            inputStream.close();
            outputStream.close();
            return new File(name);
        } catch (IOException ioe) {
            throw new RuntimeException("Error in unzipping gzipped archive " + file.getName());
        }
    }

}
