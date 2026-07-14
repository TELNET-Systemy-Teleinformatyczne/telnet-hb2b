/*
 * Copyright (C) 2026 The Holodeck B2B Team.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.holodeckb2b.core.pmode;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.holodeckb2b.common.pmode.PMode;
import org.holodeckb2b.common.pmode.Protocol;
import org.holodeckb2b.interfaces.pmode.ILeg.Label;
import org.junit.Test;

public class ProtocolTest {

    @Test
    public void parsesTLSClientCertificateAlias() throws Exception {
        final String pmodeXml =
                "<PMode xmlns=\"http://holodeck-b2b.org/schemas/2014/10/pmode\">" +
                "  <id>tls-client-cert-test</id>" +
                "  <mep>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/oneWay</mep>" +
                "  <mepBinding>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push</mepBinding>" +
                "  <Leg>" +
                "    <Protocol>" +
                "      <Address>https://example.test/as4</Address>" +
                "      <TLSConfiguration>" +
                "        <ClientCertificate>scp-sbx3-mtls</ClientCertificate>" +
                "      </TLSConfiguration>" +
                "    </Protocol>" +
                "  </Leg>" +
                "</PMode>";

        final PMode pmode = PMode.createFromXML(
                new ByteArrayInputStream(pmodeXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals("scp-sbx3-mtls", pmode.getLeg(Label.REQUEST).getProtocol().getClientCertificateAlias());
    }

    @Test
    public void copiesTLSClientCertificateAlias() {
        final Protocol source = new Protocol();
        source.setClientCertificateAlias("scp-sbx3-mtls");

        final Protocol copy = new Protocol(source);

        assertEquals("scp-sbx3-mtls", copy.getClientCertificateAlias());
    }
}
