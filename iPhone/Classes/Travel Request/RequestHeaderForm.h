//
//  RequestHeaderForm.h
//  ConcurMobile
//
//  Created by laurent mery on 18/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFFormController.h"
@class CTETravelRequest;

@interface RequestHeaderForm : FFFormController

-(void)initFormWithDatas:(CTETravelRequest*)datas;

@end
