//
//  PickerDelegate.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 17/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol PickerDelegate <NSObject>

-(void)pickerSelectionChangedToRow:(int)row tag:(id)sender;

@end
