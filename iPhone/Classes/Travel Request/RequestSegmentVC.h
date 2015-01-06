//
//  RequestSegmentVC.h
//  ConcurMobile
//
//  Created by Laurent Mery on 04/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCViewController.h"
@class CTETravelRequest;
@class CTETravelRequestEntry;

@interface RequestSegmentVC : CCViewController

@property (retain, nonatomic) CTETravelRequest *request;
@property (retain, nonatomic) CTETravelRequestEntry *entry;

@end
