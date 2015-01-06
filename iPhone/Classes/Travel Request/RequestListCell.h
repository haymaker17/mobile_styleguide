//
//  RequestListCell.h
//  ConcurMobile
//
//  Created by Laurent Mery on 8/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
@class CTETravelRequest;


@interface RequestListCell : UITableViewCell

-(CTETravelRequest*)getRequest;
-(void)updateCellWithRequestDatas:(CTETravelRequest *)request;

@end
