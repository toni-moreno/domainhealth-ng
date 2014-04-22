//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.backend.retriever;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import javax.management.openmbean.CompositeData;
import java.lang.management.MemoryUsage;

import java.util.Set;
import java.util.Iterator;


import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;

import static domainhealth.core.jmx.JavaMBeanPropConstants.*;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import static domainhealth.core.statistics.StatisticsStorage.*;
import static domainhealth.core.statistics.MonitorProperties.*;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.DateUtil;

public class HeaderLine {

        /**
         * Construct the single header line to go in a CSV file, from a list of
         * attribute names.
         *
         * @param attrList List of attributes
         * @param estLength Approximate lenght of line
         * @return The new header text line
         */
        public HeaderLine(String[] attrList) {
                headerLine = new StringBuilder(DEFAULT_HEADER_LINE_LEN);
                headerLine.append(DATE_TIME + SEPARATOR);

                for (String attr : attrList) {
                        headerLine.append(attr + SEPARATOR);
                }
                header_string=headerLine.toString();

        }

        private StringBuilder headerLine;
        public String   header_string;

        public String getString() {
                return header_string;
        }

        private static final int DEFAULT_HEADER_LINE_LEN = 100;



}

