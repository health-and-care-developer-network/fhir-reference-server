/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileLoaderTest {

    @Test
    public void testRemoveFileExtension() {

        String test1 = "thisIs_a_filename.txt";
        String test1e = "thisIs_a_filename";

        String test2 = "thisIs.a.filename.with.lots.of.dots.txt";
        String test2e = "thisIs.a.filename.with.lots.of.dots";

        String test3 = "filename with spaces.txt";
        String test3e = "filename with spaces";

        String test4 = "filename with spaces.and.dots.txt";
        String test4e = "filename with spaces.and.dots";

        String test5 = "filenameWithNoExtension";
        String test5e = "filenameWithNoExtension";

        assertEquals(FileLoader.removeFileExtension(test1), test1e);
        assertEquals(FileLoader.removeFileExtension(test2), test2e);
        assertEquals(FileLoader.removeFileExtension(test3), test3e);
        assertEquals(FileLoader.removeFileExtension(test4), test4e);
        assertEquals(FileLoader.removeFileExtension(test5), test5e);
    }

}
