//
//  RequestSegmentForm.h
//  ConcurMobile
//
//  Created by Laurent Mery on 04/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFFormController.h"

@interface RequestSegmentForm : FFFormController

-(void)initFormWithDatas:(NSArray*)datas;
-(void)reloadDatas:(NSArray*)datas;

@end
