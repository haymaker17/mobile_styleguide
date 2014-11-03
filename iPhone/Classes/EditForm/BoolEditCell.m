//
//  BoolEditCell.m
//  ConcurMobile
//
//  Created by yiwen on 5/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "BoolEditCell.h"
#import "FormFieldCell.h"

@implementation BoolEditCell
@synthesize value, switchCtrl, label, context;
@synthesize delegate = _delegate;


- (void)setSeedData:(BOOL)val delegate:(id<BoolEditDelegate>)del context:(NSObject*)theContext 
              label:(NSString*) lbl
{
    self.delegate = del;
    self.value = val;
    self.context = theContext;
    self.label.textColor = [FormFieldCell getLabelColor];
    self.label.text = lbl;
    self.switchCtrl.on = val;
}

-(IBAction) switchChanged:(id)sender
{
    self.value = self.switchCtrl.on;
    
    if (self.delegate != nil)
        [self.delegate boolUpdated:context withValue:value];
}


@end
