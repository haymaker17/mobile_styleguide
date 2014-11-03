//
//  CustomFieldTextEditor.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HotelTextEditorViewController.h"
#import "EntityTravelCustomFieldAttribute.h"
#import "EntityTravelCustomFields.h"

@interface CustomFieldTextEditor : HotelTextEditorViewController

@property (nonatomic, strong) EntityTravelCustomFields *tcf;
@end
