import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IBadge } from 'app/shared/model/badge.model';

type EntityResponseType = HttpResponse<IBadge>;
type EntityArrayResponseType = HttpResponse<IBadge[]>;

@Injectable({ providedIn: 'root' })
export class BadgeService {
    public resourceUrl = SERVER_API_URL + 'api/badges';

    constructor(protected http: HttpClient) {}

    create(badge: IBadge): Observable<EntityResponseType> {
        const copy = this.convertDateFromClient(badge);
        return this.http
            .post<IBadge>(this.resourceUrl, copy, { observe: 'response' })
            .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
    }

    update(badge: IBadge): Observable<EntityResponseType> {
        const copy = this.convertDateFromClient(badge);
        return this.http
            .put<IBadge>(this.resourceUrl, copy, { observe: 'response' })
            .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
    }

    find(id: number): Observable<EntityResponseType> {
        return this.http
            .get<IBadge>(`${this.resourceUrl}/${id}`, { observe: 'response' })
            .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
    }

    query(req?: any): Observable<EntityArrayResponseType> {
        const options = createRequestOption(req);
        return this.http
            .get<IBadge[]>(this.resourceUrl, { params: options, observe: 'response' })
            .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
    }

    delete(id: number): Observable<HttpResponse<any>> {
        return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    protected convertDateFromClient(badge: IBadge): IBadge {
        const copy: IBadge = Object.assign({}, badge, {
            availableUntil: badge.availableUntil != null && badge.availableUntil.isValid() ? badge.availableUntil.toJSON() : null
        });
        return copy;
    }

    protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
        if (res.body) {
            res.body.availableUntil = res.body.availableUntil != null ? moment(res.body.availableUntil) : null;
        }
        return res;
    }

    protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
        if (res.body) {
            res.body.forEach((badge: IBadge) => {
                badge.availableUntil = badge.availableUntil != null ? moment(badge.availableUntil) : null;
            });
        }
        return res;
    }
}
