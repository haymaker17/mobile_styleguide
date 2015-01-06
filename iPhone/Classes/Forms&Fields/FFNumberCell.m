//
//  FFNumberCell.m
//  ConcurMobile
//
//  Created by Laurent Mery on 09/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells-private.h"

@implementation FFNumberCell


#pragma mark - init

NSString *const FFCellReuseIdentifierNumber = @"NumberCell";



#pragma mark - Component

-(UITextField*)createInputValue{
    
    UITextField *input = [super createInputValue];
    
    [input setKeyboardType:UIKeyboardTypeNumbersAndPunctuation];
    
    return input;
}


@end
