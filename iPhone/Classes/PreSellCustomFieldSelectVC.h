//
//  PreSellOptionsViewController.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 09/10/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HotelOptionsViewController.h"
#import "PreSellCustomField.h"
#import "PreSellCustomFieldSelectOption.h"

@interface PreSellCustomFieldSelectVC : HotelOptionsViewController <UITableViewDelegate, UITableViewDataSource>
@property (nonatomic, strong) PreSellCustomField *tcf;
@end
