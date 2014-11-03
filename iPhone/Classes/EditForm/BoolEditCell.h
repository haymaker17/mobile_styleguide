//
//  BoolEditCell.h
//  ConcurMobile
//
//  Created by yiwen on 5/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BoolEditDelegate.h"

@interface BoolEditCell : UITableViewCell 
{
	UILabel					*label;
	BOOL                    value;
    UISwitch                *switchCtrl;
    // Formfield or rowKey, pointing to the object it is editing
    NSObject                *context;
    id<BoolEditDelegate>	__weak _delegate;
}

@property (nonatomic, strong) IBOutlet UISwitch         *switchCtrl;
@property (nonatomic, strong) IBOutlet UILabel          *label;
@property (nonatomic, strong) NSObject                  *context;
@property BOOL                                          value;
@property (nonatomic, weak) id<BoolEditDelegate>		delegate;

- (void)setSeedData:(BOOL)val delegate:(id<BoolEditDelegate>)del context:(NSObject*)context 
                label:(NSString*) label;

@end
