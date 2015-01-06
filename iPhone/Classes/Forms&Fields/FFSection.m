//
//  FFSection.m
//  ConcurMobile
//
//  Created by laurent mery on 30/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFSection.h"
#import "FFField.h"

@implementation FFSection

-(void)setFields:(NSArray*)fields{
    
    NSMutableArray *allFields = [NSMutableArray arrayWithArray:fields];
    
    NSMutableArray *fieldsVisible = [[NSMutableArray alloc]init];
    NSMutableArray *fieldsHidden = [[NSMutableArray alloc]init];
    
    for (FFField *field in allFields){
        
        if ([field isVisible]){
            
            [fieldsVisible addObject:field];
        }
        else {
            
            [fieldsHidden addObject:field];
        }
    }
    
    _fieldsHidden = [fieldsHidden copy];
    _fields = [fieldsVisible copy];
}

@end
