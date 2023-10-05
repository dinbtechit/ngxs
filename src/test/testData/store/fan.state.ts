import {Injectable} from '@angular/core';
import {State, Action, StateContext, Selector} from '@ngxs/store';

import {FanSettings} from "../../content.component";
import {FanHttpService} from "../../services/fan-http.service";

export interface FanStateModel {
  settings: FanSettings;
  httpError: HttpError;
}

export interface HttpError {
  status: boolean;
  message?: string;
}

const defaults: FanStateModel = {
  settings: {
    speed: 0, direction: true
  },
  httpError: {status: false}
};

@State<FanStateModel>({
  name: 'fan',
  defaults
})
@Injectable()
export class FanState {

  constructor(private fanHttpService: FanHttpService) {
  }

  @Action(NewAction)
  methodName({ patchState }: StateContext<FanStateModel>) {
      // TODO - Implement action
  }

  @Action(ActionWithPayload)
  actionWithPayload({ patchState }: StateContext<FanStateModel>, payload: ActionWithPayload) {
    // TODO - Implement action
  }

}


