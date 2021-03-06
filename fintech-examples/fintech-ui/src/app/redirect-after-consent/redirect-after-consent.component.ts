import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ConsentAuthorizationService } from '../bank/services/consent-authorization.service';
import { Consent } from '../models/consts';

@Component({
  selector: 'app-redirect-after-consent',
  templateUrl: './redirect-after-consent.component.html',
  styleUrls: ['./redirect-after-consent.component.scss']
})
export class RedirectAfterConsentComponent implements OnInit {
  constructor(private authService: ConsentAuthorizationService, private route: ActivatedRoute) {}

  ngOnInit() {
    const redirectCode = this.route.snapshot.queryParams.redirectCode;

    this.authService.fromConsentOk(Consent.OK, redirectCode);
  }
}
