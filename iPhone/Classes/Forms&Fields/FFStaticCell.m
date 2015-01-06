//
//  FFStaticCell.m
//  ConcurMobile
//
//  Created by laurent mery on 24/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells-private.h"



@implementation FFStaticCell


NSString *const FFCellReuseIdentifierStatic = @"StaticCell";


#pragma mark - init

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
	
	if (self = [super initWithStyle:style
					reuseIdentifier:reuseIdentifier]){
		
	}
	return self;
}

#pragma mark - layout

-(void)render{
    
    [super render];
    
    //create labelValue
    _labelValue = [self createLabelValue];
    [self.contentView addSubview:_labelValue];
}

-(void)initLayout{
    
    [super initLayout];
    
    //init constraints
    [self.constraintsElements setObject:_labelValue forKey:@"labelValue"];
    
    //set constraints
    [self addCellVisualFormatConstraints:@{
                                           @"HValue": @"H:|-margeLeft-[labelValue]-margeRight-|",
                                           @"VValue": @"V:[labelValue(labelHeight)]-margeBottom-[lineSeparator(lineSeparatorHeight)]|"
                                           }];
}


-(void)doLayout{
    
    [super doLayout];
    
    [_labelValue needsUpdateConstraints];
}


#pragma mark - component

-(UILabel*)createLabelValue{
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectNull];
    
    label.translatesAutoresizingMaskIntoConstraints = NO;
    
    [label setBackgroundColor:[UIColor clearColor]];
    [label setTextColor:[UIColor textFormInput]];
    [label setFont:[UIFont fontWithName:@"HelveticaNeue" size:18.0]];
    
    return label;
}


//public
-(void)updateDataType{
	
    [super updateDataType];
    
	[_labelValue setText:[self.field.dataType stringValue]];

}


@end
