import { Component, Input } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Subject } from 'rxjs/Subject';

import { WebsocketService, Device, Data, Config, TemplateHelper } from '../../../shared/shared';
import { CustomFieldDefinition } from '../../../shared/type/customfielddefinition';

@Component({
  selector: 'fieldstatus',
  templateUrl: './fieldstatus.component.html'
})
export class FieldstatusComponent {

  @Input()
  public currentData: Data;

  @Input()
  public fielddefinition: CustomFieldDefinition;

  constructor(public tmpl: TemplateHelper) { }
}