package io.telekom.orchest.api.core.adapters.data.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Email distribution list for an alert (TO / CC / BCC). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRecipients {

  /** Primary recipients (TO). */
  @Builder.Default private List<String> to = new ArrayList<>();

  /** Carbon-copy recipients (CC). */
  @Builder.Default private List<String> cc = new ArrayList<>();

  /** Blind carbon-copy recipients (BCC). */
  @Builder.Default private List<String> bcc = new ArrayList<>();
}
